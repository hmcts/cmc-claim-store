package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediationReportServiceTest {
    private static final String FROM_ADDRESS = "sender@mail.com";
    private static final String TO_ADDRESS = "recipient@mail.com";
    private static final String AUTHORISATION = "Authorisation";

    private static final Claim SAMPLE_CLAIM = SampleClaim.builder()
        .withResponse(SampleResponse.FullDefence.validDefaults())
        .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
        .build();

    @Mock
    private EmailService emailService;
    @Mock
    private CaseSearchApi caseSearchApi;
    @Mock
    private UserService userService;
    @Mock
    private AppInsights appInsights;
    @Captor
    private ArgumentCaptor<EmailData> emailDataCaptor;

    private MediationReportService service;

    @Before
    public void setUp() {
        this.service = new MediationReportService(
            emailService,
            caseSearchApi,
            userService,
            appInsights,
            TO_ADDRESS,
            FROM_ADDRESS
        );
        when(caseSearchApi.getMediationClaims(anyString(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(SAMPLE_CLAIM));
    }

    @Test
    public void shouldPrepareCSVDataAndInvokeEmailService() throws IOException {
        service.sendMediationReport(AUTHORISATION, LocalDate.now());

        verify(caseSearchApi).getMediationClaims(AUTHORISATION, LocalDate.now());
        verifyEmailData();
    }

    @Test
    public void automatedCSVShouldUseYesterdayAndAnonymousUser() throws IOException {
        final User mockUser = mock(User.class);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(mockUser);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);

        service.automatedMediationReport();

        verify(userService).authenticateAnonymousCaseWorker();
        verify(caseSearchApi).getMediationClaims(AUTHORISATION, LocalDate.now().minusDays(1));

        verifyEmailData();
    }

    @Test
    public void shouldReportAppInsightsEventOnException() {
        final User mockUser = mock(User.class);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(mockUser);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);

        when(caseSearchApi.getMediationClaims(anyString(), any(LocalDate.class)))
            .thenThrow(mock(RuntimeException.class));

        assertThatThrownBy(() -> service.automatedMediationReport())
            .isInstanceOf(MediationCSVGenerationException.class);

        verify(appInsights).trackEvent(eq(AppInsightsEvent.MEDIATION_REPORT_FAILURE), anyString(), any());
    }

    @Test
    public void shouldReportAppInsightsEventOnProblematicRecords() {
        User mockUser = mock(User.class);
        Claim claimWithNoClaimData = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .withClaimData(SampleClaimData.builder().withAmount(new NotKnown()).build())
            .build();
        when(caseSearchApi.getMediationClaims(anyString(), any(LocalDate.class)))
            .thenReturn(Collections.singletonList(claimWithNoClaimData));
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(mockUser);
        when(mockUser.getAuthorisation()).thenReturn(AUTHORISATION);

        service.automatedMediationReport();

        verify(appInsights).trackEvent(
            eq(AppInsightsEvent.MEDIATION_REPORT_FAILURE),
            anyString(),
            eq("{000MC001=Unable to find total amount of claim}"));
    }

    private static String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void verifyEmailData() throws IOException {
        verify(emailService).sendEmail(eq(FROM_ADDRESS), emailDataCaptor.capture());

        EmailData emailData = emailDataCaptor.getValue();
        assertThat(emailData.getSubject()).startsWith("MediationCSV");
        assertThat(emailData.getMessage()).startsWith("OCMC mediation");
        assertThat(emailData.getTo()).isEqualTo(TO_ADDRESS);

        assertThat(emailData.getAttachments()).hasSize(1);
        EmailAttachment attachment = emailData.getAttachments().get(0);
        assertThat(attachment.getFilename()).startsWith("MediationCSV");
        assertThat(attachment.getFilename()).endsWith(".csv");
        assertThat(attachment.getContentType()).isEqualTo("text/csv");

        //noinspection OptionalGetWithoutIsPresent
        assertThat(inputStreamToString(attachment.getData().getInputStream()))
            .contains(SAMPLE_CLAIM.getReferenceNumber())
            .contains(SAMPLE_CLAIM.getTotalClaimAmount().get().toString());
    }
}

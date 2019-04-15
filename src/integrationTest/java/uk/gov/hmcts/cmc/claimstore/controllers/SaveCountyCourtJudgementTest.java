package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveCountyCourtJudgementTest extends BaseIntegrationTest {

    private static final CountyCourtJudgment COUNTY_COURT_JUDGMENT
        = SampleCountyCourtJudgment
        .builder()
        .ccjType(CountyCourtJudgmentType.DEFAULT)
        .paymentOption(PaymentOption.IMMEDIATELY)
        .build();

    @MockBean
    private CCJStaffNotificationHandler ccjStaffNotificationHandler;

    @Captor
    private ArgumentCaptor<CountyCourtJudgmentEvent> countyCourtJudgementEventArgument;

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), SUBMITTER_ID, LocalDate.now());

        claimStore.updateResponseDeadline(claim.getExternalId());

        UserDetails defendantDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        UserDetails claimantDetails = SampleUserDetails.builder()
            .withUserId(SUBMITTER_ID)
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(defendantDetails);
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(claimantDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, defendantDetails));
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, claimantDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);
    }

    @Test
    public void shouldSaveCountyCourtJudgementRequest() throws Exception {

        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());

        Claim claimWithCCJRequest = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithCCJRequest.getCountyCourtJudgmentRequestedAt()).isNotNull();
    }

    @Test
    public void shouldInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());

        verify(ccjStaffNotificationHandler)
            .onDefaultJudgmentRequestSubmitted(countyCourtJudgementEventArgument.capture());

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);
        assertThat(countyCourtJudgementEventArgument.getValue().getClaim()).isEqualTo(updatedClaim);
    }

    @Test
    public void shouldUploadDocumentToDocumentManagementAfterSuccessfulSave() throws Exception {
        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        InMemoryMultipartFile ccj = new InMemoryMultipartFile(
            "files",
            buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()) + ".pdf",
            MediaType.APPLICATION_PDF_VALUE,
            PDF_BYTES
        );
        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());
        verify(documentUploadClient).upload(anyString(),
            anyString(),
            anyString(),
            anyList(),
            any(Classification.class),
            argument.capture());
        List<MultipartFile> files = argument.getValue();
        assertTrue(files.contains(ccj));
    }

    @Test
    public void shouldNotReturn500HttpStatusWhenUploadDocumentToDocumentManagementFails() throws Exception {
        given(documentUploadClient.upload(eq(AUTHORISATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());
        Claim claimWithCCJRequest = claimStore.getClaimByExternalId(claim.getExternalId());
        assertThat(claimWithCCJRequest.getCountyCourtJudgmentRequestedAt()).isNotNull();
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());

        verify(notificationClient, times(1))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    private ResultActions makeRequest(String externalId, CountyCourtJudgment countyCourtJudgment) throws Exception {
        String path = "/claims/" + externalId + "/county-court-judgment";

        return webClient
            .perform(post(path)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(countyCourtJudgment))
            );
    }
}

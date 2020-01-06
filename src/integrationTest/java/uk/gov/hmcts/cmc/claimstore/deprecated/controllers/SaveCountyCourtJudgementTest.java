package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
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
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

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

        given(documentUploadClient
            .upload(eq(AUTHORISATION_TOKEN), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
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

        assertThat(countyCourtJudgementEventArgument.getValue().getClaim().getCountyCourtJudgment())
            .isEqualTo(updatedClaim.getCountyCourtJudgment());

        assertThat(updatedClaim.getCountyCourtJudgmentRequestedAt()).isNotNull();
    }

    @Test
    public void shouldNotUploadDocumentToDocumentManagementAfterSuccessfulSave() throws Exception {

        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().isOk());
        verify(documentUploadClient, never()).upload(anyString(),
            anyString(),
            anyString(),
            anyList(),
            any(Classification.class),
            anyList());
    }

    @Test
    public void shouldNotReturn500HttpStatusWhenUploadDocumentToDocumentManagementFails() throws Exception {
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

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        makeRequest(claim.getExternalId(), COUNTY_COURT_JUDGMENT).andExpect(status().is5xxServerError());

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), contains("claimant-ccj-requested-notification-"));

        verify(appInsights).trackEvent(
            eq(NOTIFICATION_FAILURE),
            eq(REFERENCE_NUMBER),
            eq("claimant-ccj-requested-notification-" + claim.getReferenceNumber())
        );
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidJudgementIsSubmitted() throws Exception {
        CountyCourtJudgment invalidCCJ = SampleCountyCourtJudgment.builder()
            .paymentOption(null)
            .build();

        makeRequest(claim.getExternalId(), invalidCCJ)
            .andExpect(status().isUnprocessableEntity());
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

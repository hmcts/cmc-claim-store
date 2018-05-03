package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails.getDefault;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false",
        "feature_toggles.emailToStaff=false"
    }
)
public class StaffEmailServiceWithNotificationDisabledTest extends BaseSaveTest {

    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;
    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Before
    public void setUp() {
        given(userService.getUserDetails(anyString())).willReturn(getDefault());
    }

    @Test
    public void shouldNotSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    @Test
    public void shouldNotSendStaffNotificationWhenCCJRequestSubmitted() throws Exception {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder().build();

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        makeRequest(claim.getExternalId(), countyCourtJudgment).andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeRequest(String externalId, CountyCourtJudgment countyCourtJudgment) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/county-court-judgment")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(countyCourtJudgment))
            );
    }

    @Test
    public void shouldNotSendStaffNotificationWhenCounterSignRequestSubmitted() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        caseRepository.saveDefendantResponse(claim, SampleClaim.DEFENDANT_EMAIL, response, BEARER_TOKEN);

        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), MadeBy.DEFENDANT);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, "OFFER_MADE_BY_DEFENDANT");

        settlement.accept(MadeBy.CLAIMANT);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, "OFFER_ACCEPTED_BY_CLAIMANT");

        makeCounterOfferSignedRequest(claim.getExternalId()).andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeCounterOfferSignedRequest(String externalId) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/offers/CLAIMANT/countersign")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
            );
    }

    @Test
    public void shouldNotInvokeStaffActionsHandlerAfterSuccessfulDefendantResponseSave() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        makeDefendantResponseRequest(claim.getExternalId(), response).andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    @Test
    public void shouldNotSendStaffNotificationsForDefendantRequestedMoreTimeEvent() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);
        Claim updated = SampleClaim.builder().withResponseDeadline(newDeadline).withClaimData(claim.getClaimData())
            .withReferenceNumber(claim.getReferenceNumber())
            .withExternalId(claim.getExternalId()).build();

        makeRequestForMoreTimeToRespond(claim.getExternalId(), updated)
            .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeDefendantResponseRequest(String externalId, Response response) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + DEFENDANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(response))
            );
    }

    private ResultActions makeRequestForMoreTimeToRespond(String externalId, Claim claim) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/request-more-time")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claim))
            );
    }
}

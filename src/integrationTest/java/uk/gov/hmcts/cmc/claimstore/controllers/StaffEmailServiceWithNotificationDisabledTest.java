package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails.getDefault;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "feature_toggles.ccd_enabled=false",
        "feature_toggles.emailToStaff=false",
        "feature_toggles.async_event_operations_enabled=false"
    }
)
public class StaffEmailServiceWithNotificationDisabledTest extends BaseSaveTest {

    public static final String DEFENDANT_BEARER_TOKEN = "defendant-token";
    private static final User USER = new User(BEARER_TOKEN, getDefault());

    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;
    @Captor
    private ArgumentCaptor<String> senderArgument;

    private Claim claim;

    @Before
    public void setUp() {
        given(userService.getUserDetails(anyString())).willReturn(getDefault());

        claim = SampleClaim.builder()
            .withExternalId(UUID.randomUUID().toString())
            .withClaimData(SampleClaimData.validDefaults())
            .withIssuedOn(LocalDate.now().minusDays(3))
            .withResponseDeadline(LocalDate.now().minusDays(1))
            .withSubmitterId("1")
            .withDefendantId(DEFENDANT_ID)
            .build();

        claim = caseRepository.saveClaim(USER, claim, CaseEvent.CREATE_CASE);
    }

    @Test
    public void shouldNotSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    @Test
    public void shouldNotSendStaffNotificationWhenDefaultCCJRequestSubmitted() throws Exception {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        makeRequest(claim.getExternalId(), countyCourtJudgment).andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeRequest(String externalId, CountyCourtJudgment countyCourtJudgment) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/county-court-judgment")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(countyCourtJudgment))
            );
    }

    @Test
    public void shouldNotSendStaffNotificationWhenCounterSignRequestSubmitted() throws Exception {
        claimStore.saveResponse(claim, SampleResponse.FullDefence.builder().build());
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT, null);
        claim = claimStore.makeOffer(claim.getExternalId(), settlement);

        settlement.accept(MadeBy.CLAIMANT, null);
        claim = claimStore.acceptOffer(claim.getExternalId(), settlement);

        makeCounterOfferSignedRequest(claim.getExternalId()).andExpect(status().isCreated());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeCounterOfferSignedRequest(String externalId) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/offers/DEFENDANT/countersign")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }

    @Test
    public void shouldNotInvokeStaffActionsHandlerAfterSuccessfulDefendantResponseSave() throws Exception {
        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
            .build();

        User citizen = SampleUser.builder()
            .withAuthorisation(BEARER_TOKEN)
            .withUserDetails(userDetails)
            .build();

        given(userService.getUser(BEARER_TOKEN)).willReturn(citizen);
        given(userService.getUser(DEFENDANT_BEARER_TOKEN)).willReturn(new User(DEFENDANT_BEARER_TOKEN, userDetails));

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendant(BEARER_TOKEN);

        Response response = SampleResponse.validDefaults();
        makeDefendantResponseRequest(claim.getExternalId(), response).andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    @Test
    public void shouldNotSendStaffNotificationsForDefendantRequestedMoreTimeEvent() throws Exception {
        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail("defendant@example.com")
            .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
            .build();

        given(userService.getUser(DEFENDANT_BEARER_TOKEN)).willReturn(new User(DEFENDANT_BEARER_TOKEN, userDetails));
        given(userService.getUserDetails(DEFENDANT_BEARER_TOKEN)).willReturn(userDetails);
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now().plusDays(1));
        caseRepository.linkDefendant(DEFENDANT_BEARER_TOKEN);

        makeRequestForMoreTimeToRespond(claim.getExternalId(), claim)
            .andExpect(status().isOk());

        verify(emailService, never()).sendEmail(senderArgument.capture(), emailDataArgument.capture());
    }

    private ResultActions makeDefendantResponseRequest(String externalId, Response response) throws Exception {
        return webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + DEFENDANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, DEFENDANT_BEARER_TOKEN)
                .content(jsonMapper.toJson(response))
            );
    }

    private ResultActions makeRequestForMoreTimeToRespond(String externalId, Claim claim) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/request-more-time")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, DEFENDANT_BEARER_TOKEN)
                .content(jsonMapper.toJson(claim))
            );
    }
}

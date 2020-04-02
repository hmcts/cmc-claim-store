package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_SIGNED_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;

public class ClaimantResponseTest extends BaseMockSpringTest {
    private static final String RESPONSE_URL = "/responses/{externalId}/claimant/{claimantId}";

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected ClaimService claimService;

    @MockBean
    protected CaseRepository caseRepository;

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder()
                .withUserId(SampleClaim.USER_ID)
                .withMail(SampleClaim.SUBMITTER_EMAIL)
                .withRoles(Role.CITIZEN.getRole())
                .build());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void testForbiddenWhenNotYourClaim() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .submitterId(SampleClaim.USER_ID + "diff")
            .build();
        submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim, claim
        ).andExpect(
            status().isForbidden()
        );
    }

    @Test
    public void testForbiddenWhenDefendantResponseMissing() throws Exception {
        Claim claim = SampleClaim.getDefaultWithoutResponse(SampleClaim.DEFENDANT_EMAIL);
        submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim, claim
        ).andExpect(
            status().isForbidden()
        );
    }

    @Test
    public void testConflictWhenClaimantResponseAlreadySaved() throws Exception {
        Claim claim = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
        submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim, claim
        ).andExpect(
            status().isConflict()
        );
    }

    @Test
    public void testLiftStayWhenAdmittedAndStayed() throws Exception {
        Claim stayedClaim = SampleClaim.getClaimWithPartAdmissionAndNoMediation().toBuilder()
            .state(ClaimState.STAYED).build();
        Claim liftedClaim = stayedClaim.toBuilder()
            .state(ClaimState.OPEN).build();
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();
        Claim updatedClaim = liftedClaim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        when(caseRepository.saveCaseEvent(AUTHORISATION_TOKEN, stayedClaim, CaseEvent.LIFT_STAY))
            .thenReturn(liftedClaim);

        when(claimService.getClaimByExternalId(stayedClaim.getExternalId(), AUTHORISATION_TOKEN))
            .thenReturn(stayedClaim, liftedClaim);

        when(caseRepository.saveClaimantResponse(liftedClaim, claimantResponse, AUTHORISATION_TOKEN))
            .thenReturn(updatedClaim);

        doPost(AUTHORISATION_TOKEN, claimantResponse, RESPONSE_URL, SampleClaim.EXTERNAL_ID, SampleClaim.USER_ID)
            .andExpect(status().isCreated());

        verify(caseRepository)
            .saveCaseEvent(AUTHORISATION_TOKEN, stayedClaim, CaseEvent.LIFT_STAY);
    }

    @Test
    public void testFormaliseRepaymentPlanWithSettlement() throws Exception {
        Claim updatedClaim = testFormaliseRepaymentPlan(FormaliseOption.SETTLEMENT);

        verify(caseRepository).updateSettlement(
            eq(updatedClaim),
            any(Settlement.class),
            eq(AUTHORISATION_TOKEN),
            eq(AGREEMENT_SIGNED_BY_CLAIMANT)
        );
    }

    @Test
    public void testFormaliseRepaymentPlanWithCCJ() throws Exception {
        Claim updatedClaim = testFormaliseRepaymentPlan(FormaliseOption.CCJ);

        verify(claimService).saveCountyCourtJudgment(
            eq(AUTHORISATION_TOKEN),
            eq(updatedClaim),
            any(CountyCourtJudgment.class)
        );
    }

    @Test
    public void testFormaliseRepaymentPlanWithReferToJudge() throws Exception {
        Claim updatedClaim = testFormaliseRepaymentPlan(FormaliseOption.REFER_TO_JUDGE);

        verify(caseRepository).saveCaseEvent(
            AUTHORISATION_TOKEN,
            updatedClaim,
            INTERLOCUTORY_JUDGMENT
        );
    }

    @Test
    public void testStayClaimOnFullDefenseDisputeAcceptation() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();
        ResponseAcceptation claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        Claim updatedClaim = claim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        submitClaimantResponse(
            claimantResponse,
            claim, updatedClaim
        ).andExpect(
            status().isCreated()
        );

        verify(caseRepository)
            .saveCaseEvent(AUTHORISATION_TOKEN, updatedClaim, CaseEvent.STAY_CLAIM);
    }

    @Test
    public void testUpdateDQDeadlineOnRejectOfflineDQNoMediation() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullAdmission().toBuilder()
            .features(Collections.emptyList()).build();
        ResponseRejection claimantResponse = SampleClaimantResponse.validDefaultRejection().toBuilder()
            .freeMediation(YesNoOption.NO).build();

        Claim updatedClaim = claim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        submitClaimantResponse(
            claimantResponse,
            claim, updatedClaim
        ).andExpect(
            status().isCreated()
        );

        verify(caseRepository).updateDirectionsQuestionnaireDeadline(
            eq(claim),
            any(LocalDate.class),
            eq(AUTHORISATION_TOKEN)
        );
    }

    @Test
    public void testSettledPreJudgment() throws Exception {
        Claim claim = SampleClaim.getWithResponse(SampleResponse.FullDefence.validDefaults().toBuilder()
            .defenceType(DefenceType.ALREADY_PAID)
            .paymentDeclaration(PaymentDeclaration.builder().paidAmount(BigDecimal.TEN)
                .paidDate(LocalDate.now()).explanation("explanation").build()).build()).toBuilder()
            .respondedAt(LocalDateTime.now()).build();
        ResponseAcceptation claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceSettlePreJudgement();

        Claim updatedClaim = claim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        submitClaimantResponse(
            claimantResponse,
            claim, updatedClaim
        ).andExpect(
            status().isCreated()
        );

        verify(caseRepository)
            .saveCaseEvent(AUTHORISATION_TOKEN, updatedClaim, CaseEvent.SETTLED_PRE_JUDGMENT);
    }

    @Test
    public void testReferredToMediation() throws Exception {
        Claim claim = SampleClaim.getWithResponse(SampleResponse.validDefaults().toBuilder()
            .freeMediation(YesNoOption.YES).build());
        ResponseRejection claimantResponse = SampleClaimantResponse.validRejectionWithFreeMediation();

        Claim updatedClaim = claim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        submitClaimantResponse(
            claimantResponse,
            claim, updatedClaim
        ).andExpect(
            status().isCreated()
        );

        verify(caseRepository)
            .saveCaseEvent(AUTHORISATION_TOKEN, updatedClaim, CaseEvent.REFERRED_TO_MEDIATION);
    }

    private Claim testFormaliseRepaymentPlan(FormaliseOption formaliseOption) throws Exception {
        Claim claim = SampleClaim.getWithResponse(
            SampleResponse.FullAdmission.builder().buildWithPaymentOptionBySpecifiedDate());
        ResponseAcceptation claimantResponse = SampleClaimantResponse.validDefaultAcceptation().toBuilder()
            .formaliseOption(formaliseOption).build();

        Claim updatedClaim = claim.toBuilder()
            .claimantResponse(claimantResponse)
            .claimantRespondedAt(LocalDateTime.now()).build();

        submitClaimantResponse(
            claimantResponse,
            claim, updatedClaim
        ).andExpect(
            status().isCreated()
        );
        return updatedClaim;
    }

    private ResultActions submitClaimantResponse(
        ClaimantResponse claimantResponse,
        Claim firstClaim,
        Claim updatedClaim
    ) throws Exception {
        when(claimService.getClaimByExternalId(firstClaim.getExternalId(), AUTHORISATION_TOKEN))
            .thenReturn(firstClaim, updatedClaim);

        when(caseRepository.saveClaimantResponse(firstClaim, claimantResponse, AUTHORISATION_TOKEN))
            .thenReturn(updatedClaim);

        return doPost(AUTHORISATION_TOKEN, claimantResponse,
            RESPONSE_URL, SampleClaim.EXTERNAL_ID, SampleClaim.USER_ID);
    }
}

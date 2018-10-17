package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.COURT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.DEFENDANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.SETTLEMENT;


@RunWith(MockitoJUnitRunner.class)
public class FormaliseResponseAcceptanceServiceTest {

    private static final String AUTH = "AUTH";

    private FormaliseResponseAcceptanceService formaliseResponseAcceptanceService;

    @Mock
    private OffersService offersService;

    @Mock
    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Captor
    private ArgumentCaptor<CountyCourtJudgment> countyCourtJudgmentArgumentCaptor;

    @Captor
    private ArgumentCaptor<Settlement> settlementArgumentCaptor;

    @Before
    public void before() {
        formaliseResponseAcceptanceService = new FormaliseResponseAcceptanceService(countyCourtJudgmentService,
            offersService);
    }

    @Test(expected = IllegalStateException.class)
    public void formaliseWhenResponseNotPresent() {
        Claim claim = SampleClaim.builder().build();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();
        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
    }

    @Test(expected = IllegalStateException.class)
    public void formaliseCCJWhenResponseIsNotAdmissions() {
        Response fullDefenceResponse = SampleResponse.FullDefence.builder().build();

        Claim claim = SampleClaim.getWithResponse(fullDefenceResponse);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
    }

    @Test(expected = IllegalStateException.class)
    public void formaliseSettlementWhenResponseIsNotAdmissions() {
        Response fullDefenceResponse = SampleResponse.FullDefence.builder().build();

        Claim claim = SampleClaim.getWithResponse(fullDefenceResponse);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void formaliseSettlementWhenNoFormaliseOptionPresent() {

        Claim claim = SampleClaim.getWithDefaultResponse();

        ResponseAcceptation responseAcceptation = ResponseAcceptation.builder().build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);
    }

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionBySetDateAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        LocalDate respondentPayingBySetDate = ((PartAdmissionResponse) partAdmissionsResponsePayBySetDate)
            .getPaymentIntention()
            .get()
            .getPaymentDate()
            .orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(respondentPayingBySetDate);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionByInstalmentsAccepted() {
        Response admissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();

        RepaymentPlan repaymentPlanOfDefendant = ((PartAdmissionResponse) admissionResponsePayByInstalments)
            .getPaymentIntention()
            .get()
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(admissionResponsePayByInstalments);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan().orElseThrow(IllegalAccessError::new))
            .isEqualTo(repaymentPlanOfDefendant);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedIntentionAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDate();
        LocalDate appliedPaymentDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(CourtDetermination
                .builder()
                .courtDecision(paymentIntention)
                .decisionType(COURT)
                .build())
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(appliedPaymentDate);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedHavingClaimantDecisionType() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDate();
        LocalDate appliedPaymentDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);

        PaymentIntention claimantPaymentIntention = SamplePaymentIntention.instalments();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(CourtDetermination
                .builder()
                .courtPaymentIntention(claimantPaymentIntention)
                .courtDecision(paymentIntention)
                .decisionType(CLAIMANT)
                .build())
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(appliedPaymentDate);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedHavingDefendantDecisionType() {
        Response partAdmissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();

        Claim claim = SampleClaim.getWithResponse(partAdmissionResponsePayByInstalments);

        PaymentIntention paymentIntention = SamplePaymentIntention.instalments();

        PaymentIntention claimantPaymentIntention = SamplePaymentIntention.bySetDate();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(CourtDetermination
                .builder()
                .courtPaymentIntention(claimantPaymentIntention)
                .courtDecision(paymentIntention)
                .decisionType(DEFENDANT)
                .build())
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));


        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate().isPresent()).isFalse();

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithClaimantPaymentIntentionPresent() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        PaymentIntention paymentIntentionByInstalments = SamplePaymentIntention.instalments();
        RepaymentPlan appliedRepaymentPlan = paymentIntentionByInstalments
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .claimantPaymentIntention(paymentIntentionByInstalments)
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(appliedRepaymentPlan);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseCCJWithFullAdmissionAndDefendantsPaymentIntention() {
        Response fullAdmissionResponseWithInstalments = SampleResponse.FullAdmission.builder().build();

        RepaymentPlan repaymentPlan = ((FullAdmissionResponse) fullAdmissionResponseWithInstalments)
            .getPaymentIntention()
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseWithInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            eq(claim.getSubmitterId()),
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH),
            eq(true));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan()
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(repaymentPlan);

        verifyZeroInteractions(offersService);
    }

    @Test
    public void formaliseSettlementWithDefendantPaymentIntentionBySetDateAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        PaymentIntention paymentIntentionOfDefendant = ((PartAdmissionResponse) partAdmissionsResponsePayBySetDate)
            .getPaymentIntention().orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithDefendantPaymentIntentionByInstalments() {
        Response partAdmissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();

        PaymentIntention paymentIntentionOfDefendant = ((PartAdmissionResponse)
            partAdmissionResponsePayByInstalments).getPaymentIntention().orElseThrow(IllegalStateException::new);

        Claim claim = SampleClaim.getWithResponse(partAdmissionResponsePayByInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithCourtDeterminedPaymentIntention() {

        Claim claim = SampleClaim.getWithDefaultResponse();

        PaymentIntention paymentIntention = SamplePaymentIntention.instalments();

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(CourtDetermination
                .builder()
                .courtDecision(paymentIntention)
                .decisionType(COURT)
                .build())
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntention);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithClaimantsPaymentIntention() {

        Claim claim = SampleClaim.getWithDefaultResponse();

        PaymentIntention paymentIntention = SamplePaymentIntention.instalments();

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .claimantPaymentIntention(paymentIntention)
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntention);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithFullAdmissionsAndDefendantsPaymentIntention() {
        Response fullAdmissionResponseWithInstalments = SampleResponse.FullAdmission.builder().build();

        PaymentIntention paymentIntentionOfDefendant = ((FullAdmissionResponse) fullAdmissionResponseWithInstalments)
            .getPaymentIntention();

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseWithInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithFullAdmissionsBySetDateAndDefendantPaymentIntention() {
        Response fullAdmissionResponseBySetDate = SampleResponse
            .FullAdmission
            .builder()
            .buildWithPaymentOptionBySpecifiedDate();

        PaymentIntention paymentIntentionOfDefendant = ((FullAdmissionResponse) fullAdmissionResponseBySetDate)
            .getPaymentIntention();

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseBySetDate);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(offersService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastOfferStatement()
            .getOffer()
            .orElseThrow(IllegalStateException::new)
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyZeroInteractions(countyCourtJudgmentService);
    }

    @Test
    public void doNotFormaliseWhenReferredToJudge() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
            .build();
        assertThatCode(() -> formaliseResponseAcceptanceService
            .formalise(claim, responseAcceptation, AUTH)).doesNotThrowAnyException();

        verifyZeroInteractions(countyCourtJudgmentService);
        verifyZeroInteractions(offersService);
    }

    private PartAdmissionResponse getPartAdmissionResponsePayByInstalments() {
        return SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionInstallments();
    }

    private Response getPartAdmissionsResponsePayBySetDate() {
        return SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();
    }
}

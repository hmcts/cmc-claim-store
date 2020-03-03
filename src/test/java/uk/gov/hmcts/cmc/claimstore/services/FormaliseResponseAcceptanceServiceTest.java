package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_OFFER;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT_INTENTION;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REPAYMENT_PLAN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
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
    private SettlementAgreementService settlementAgreementService;

    @Mock
    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private CaseRepository caseRepository;

    @Captor
    private ArgumentCaptor<CountyCourtJudgment> countyCourtJudgmentArgumentCaptor;

    @Captor
    private ArgumentCaptor<Settlement> settlementArgumentCaptor;

    @Before
    public void before() {
        formaliseResponseAcceptanceService = new FormaliseResponseAcceptanceService(
            countyCourtJudgmentService,
            settlementAgreementService,
            eventProducer,
            caseRepository
        );
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

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionBySetDateAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        LocalDate respondentPayingBySetDate = ((PartAdmissionResponse) partAdmissionsResponsePayBySetDate)
            .getPaymentIntention()
            .flatMap(PaymentIntention::getPaymentDate)
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE));

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE)))
            .isEqualTo(respondentPayingBySetDate);

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseCCJWithDefendantPaymentIntentionByInstalmentsAccepted() {
        PartAdmissionResponse admissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();

        RepaymentPlan repaymentPlanOfDefendant = admissionResponsePayByInstalments
            .getPaymentIntention()
            .flatMap(PaymentIntention::getRepaymentPlan)
            .orElseThrow(() -> new IllegalStateException(MISSING_REPAYMENT_PLAN));

        Claim claim = SampleClaim.getWithResponse(admissionResponsePayByInstalments);
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan().orElseThrow(IllegalAccessError::new))
            .isEqualTo(repaymentPlanOfDefendant);

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedIntentionAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDate();
        LocalDate appliedPaymentDate = paymentIntention.getPaymentDate()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE));

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
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE)))
            .isEqualTo(appliedPaymentDate);

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminedHavingClaimantDecisionType() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        PaymentIntention paymentIntention = SamplePaymentIntention.bySetDate();
        LocalDate appliedPaymentDate = paymentIntention.getPaymentDate()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE));

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
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE)))
            .isEqualTo(appliedPaymentDate);

        verifyNoInteractions(settlementAgreementService);
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
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getPayBySetDate().isPresent()).isFalse();

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseCCJWithCourtDeterminationPresent() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        CourtDetermination courtDeterminedPaymentPlanByInstalments = SampleCourtDetermination.instalments();

        RepaymentPlan appliedPlan = courtDeterminedPaymentPlanByInstalments
            .getCourtDecision()
            .getRepaymentPlan()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_REPAYMENT_PLAN));

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .courtDetermination(SampleCourtDetermination.instalments())
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan()
            .orElseThrow(() -> new IllegalStateException(MISSING_REPAYMENT_PLAN)))
            .isEqualTo(appliedPlan);

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseCCJWithFullAdmissionAndDefendantsPaymentIntention() {
        FullAdmissionResponse fullAdmissionResponseWithInstalments = SampleResponse.FullAdmission.builder().build();

        RepaymentPlan repaymentPlan = fullAdmissionResponseWithInstalments
            .getPaymentIntention()
            .getRepaymentPlan()
            .orElseThrow(() -> new IllegalStateException(MISSING_REPAYMENT_PLAN));

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseWithInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(CCJ)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(countyCourtJudgmentService).save(
            countyCourtJudgmentArgumentCaptor.capture(),
            eq(claim.getExternalId()),
            eq(AUTH));

        assertThat(countyCourtJudgmentArgumentCaptor
            .getValue()
            .getRepaymentPlan()
            .orElseThrow(() -> new IllegalStateException(MISSING_REPAYMENT_PLAN)))
            .isEqualTo(repaymentPlan);

        verifyNoInteractions(settlementAgreementService);
    }

    @Test
    public void formaliseSettlementWithDefendantPaymentIntentionBySetDateAccepted() {
        Response partAdmissionsResponsePayBySetDate = getPartAdmissionsResponsePayBySetDate();

        PaymentIntention paymentIntentionOfDefendant = ((PartAdmissionResponse) partAdmissionsResponsePayBySetDate)
            .getPaymentIntention()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_INTENTION));

        Claim claim = SampleClaim.getWithResponse(partAdmissionsResponsePayBySetDate);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER))
            .getPaymentIntention()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_INTENTION));

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithDefendantPaymentIntentionByInstalments() {
        PartAdmissionResponse partAdmissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();

        PaymentIntention paymentIntentionOfDefendant = partAdmissionResponsePayByInstalments.getPaymentIntention()
            .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_INTENTION));

        Claim claim = SampleClaim.getWithResponse(partAdmissionResponsePayByInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER))
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntentionOfDefendant);

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithCourtDeterminedPaymentIntentionByInstalments() {

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

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER))
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntention);

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithCourtDeterminedPaymentIntentionByImmediately() {

        Response partAdmissionResponsePayByInstalments = getPartAdmissionResponsePayByInstalments();
        Claim claim = SampleClaim.getWithResponse(partAdmissionResponsePayByInstalments);

        PaymentIntention paymentIntention = SamplePaymentIntention.immediately();

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

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        PaymentIntention paymentIntentionWithinOffer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER))
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(paymentIntention);

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithFullAdmissionsAndDefendantsPaymentIntention() {
        FullAdmissionResponse fullAdmissionResponseWithInstalments = SampleResponse.FullAdmission.builder().build();

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseWithInstalments);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        Offer offer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER));

        assertThat(offer.getContent()).contains("John Rambo will repay £81.90, in instalments of £100.99 every week."
            + " The first instalment will be paid by 10 October 2100.");

        PaymentIntention paymentIntentionWithinOffer = offer
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer)
            .isEqualTo(fullAdmissionResponseWithInstalments.getPaymentIntention());

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void formaliseSettlementWithFullAdmissionsBySetDateAndDefendantPaymentIntention() {
        FullAdmissionResponse fullAdmissionResponseBySetDate = SampleResponse
            .FullAdmission
            .builder()
            .buildWithPaymentOptionBySpecifiedDate();

        Claim claim = SampleClaim.getWithResponse(fullAdmissionResponseBySetDate);

        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(SETTLEMENT)
            .build();

        formaliseResponseAcceptanceService.formalise(claim, responseAcceptation, AUTH);

        verify(settlementAgreementService).signSettlementAgreement(
            eq(claim.getExternalId()),
            settlementArgumentCaptor.capture(),
            eq(AUTH));

        Offer offer = settlementArgumentCaptor
            .getValue()
            .getLastStatementOfType(StatementType.OFFER)
            .getOffer()
            .orElseThrow(() -> new IllegalStateException(MISSING_OFFER));

        assertThat(offer.getContent()).startsWith("John Rambo will pay £81.90");

        PaymentIntention paymentIntentionWithinOffer = offer
            .getPaymentIntention()
            .orElseThrow(IllegalAccessError::new);

        assertThat(paymentIntentionWithinOffer).isEqualTo(fullAdmissionResponseBySetDate.getPaymentIntention());

        verifyNoInteractions(countyCourtJudgmentService);
    }

    @Test
    public void createInterlocutoryJudgmentEventWhenReferredToJudge() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ResponseAcceptation responseAcceptation = ResponseAcceptation
            .builder()
            .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
            .build();
        assertThatCode(() -> formaliseResponseAcceptanceService
            .formalise(claim, responseAcceptation, AUTH)).doesNotThrowAnyException();

        verify(eventProducer, once()).createInterlocutoryJudgmentEvent(eq(claim));
        verify(caseRepository, once()).saveCaseEvent(anyString(), eq(claim), eq(INTERLOCUTORY_JUDGMENT));
        verifyNoInteractions(countyCourtJudgmentService);
        verifyNoInteractions(settlementAgreementService);
    }

    private PartAdmissionResponse getPartAdmissionResponsePayByInstalments() {
        return SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionInstalments();
    }

    private Response getPartAdmissionsResponsePayBySetDate() {
        return SampleResponse
            .PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();
    }
}

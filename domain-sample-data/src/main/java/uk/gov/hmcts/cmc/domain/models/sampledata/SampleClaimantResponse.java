package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;

import java.math.BigDecimal;

import static java.math.BigDecimal.TEN;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.REFER_TO_JUDGE;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.SETTLEMENT;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation.pilotHearingLocation;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.instalments;

public abstract class SampleClaimantResponse {

    public static ResponseAcceptation validDefaultAcceptation() {
        return ClaimantResponseAcceptation.builder().build();
    }

    public static ResponseRejection validDefaultRejection() {
        return ClaimantResponseRejection.builder().build();
    }

    public static ResponseRejection validRejectionWithFreeMediation() {
        return ClaimantResponseRejection.builder().buildRejectionWithFreeMediation();
    }

    public static ResponseRejection validRejectionWithDirectionsQuestionnaire() {
        return ClaimantResponseRejection.builder().buildRejectionWithDirectionsQuestionnaire();
    }

    public static class ClaimantResponseAcceptation extends SampleClaimantResponse {

        private BigDecimal amountPaid = TEN;
        private FormaliseOption formaliseOption = REFER_TO_JUDGE;

        public static ClaimantResponseAcceptation builder() {
            return new ClaimantResponseAcceptation();
        }

        public ClaimantResponseAcceptation withAmountPaid(BigDecimal amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        public ClaimantResponseAcceptation withFormaliseOption(FormaliseOption formaliseOption) {
            this.formaliseOption = formaliseOption;
            return this;
        }

        @Override
        public ResponseAcceptation build() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(formaliseOption)
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueCCJWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueCCJWithClaimantPaymentIntentionBySetDate() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueCCJWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueSettlementWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueSettlementWithClaimantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ResponseAcceptation buildAcceptationIssueSettlementWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }

        public ResponseAcceptation buildAcceptationReferToJudgeWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(REFER_TO_JUDGE)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }

        public ResponseAcceptation buildAcceptanceIssueSettlementWithCourtDeterminationPayByInstalments() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(instalments())
                .courtDetermination(SampleCourtDetermination.instalments())
                .build();
        }

        public ResponseAcceptation buildAcceptanceIssueSettlementWithClaimantPaymentIntentionPayImmediately() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(instalments())
                .courtDetermination(SampleCourtDetermination.instalments())
                .build();
        }

        public ResponseAcceptation buildAcceptanceSettlePreJudgement() {
            return ResponseAcceptation.builder()
                .paymentReceived(YES)
                .settleForAmount(YES)
                .build();
        }
    }

    public static class ClaimantResponseRejection extends SampleClaimantResponse {

        public static ClaimantResponseRejection builder() {
            return new ClaimantResponseRejection();
        }

        @Override
        public ResponseRejection build() {
            return ResponseRejection.builder()
                .amountPaid(TEN)
                .freeMediation(NO)
                .reason("Some valid reason")
                .build();
        }

        public ResponseRejection buildRejectionSettlePreJudgement() {
            return ResponseRejection.builder()
                .paymentReceived(YES)
                .settleForAmount(NO)
                .build();
        }

        public ResponseRejection buildRejectionWithFreeMediation() {
            return ResponseRejection.builder()
                .amountPaid(TEN)
                .freeMediation(YES)
                .mediationPhoneNumber("07999999999")
                .mediationContactPerson("Mediation Contact Person")
                .reason("Some valid reason")
                .build();
        }

        public ResponseRejection buildRejectionWithDirectionsQuestionnaire() {
            return ResponseRejection.builder()
                .amountPaid(TEN)
                .freeMediation(NO)
                .mediationPhoneNumber("07999999999")
                .mediationContactPerson("Mediation Contact Person")
                .reason("Some valid reason")
                .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder()
                    .withHearingLocation(pilotHearingLocation).build())
                .build();
        }
    }

    public abstract ClaimantResponse build();
}

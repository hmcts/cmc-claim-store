package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.COURT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.DEFENDANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.REFER_TO_JUDGE;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.SETTLEMENT;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

public abstract class SampleClaimantResponse<T extends SampleClaimantResponse<T>> {

    public static ClaimantResponse validDefaultAcceptation() {
        return ClaimantResponseAcceptation.builder().build();
    }

    public static ClaimantResponse validDefaultRejection() {
        return ClaimantResponseRejection.builder().build();
    }

    public static class ClaimantResponseAcceptation extends SampleClaimantResponse<ClaimantResponseAcceptation> {

        private BigDecimal amountPaid = BigDecimal.TEN;
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
        public ClaimantResponse build() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(formaliseOption)
                .decisionType(DEFENDANT)
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .decisionType(DEFENDANT)
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithClaimantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .decisionType(CLAIMANT)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .decisionType(COURT)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .decisionType(DEFENDANT)
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithClaimantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .decisionType(CLAIMANT)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .decisionType(COURT)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }
    }

    public static class ClaimantResponseRejection extends SampleClaimantResponse<ClaimantResponseAcceptation> {

        public static ClaimantResponseRejection builder() {
            return new ClaimantResponseRejection();
        }

        @Override
        public ClaimantResponse build() {
            return ResponseRejection.builder()
                .amountPaid(BigDecimal.TEN)
                .freeMediation(false)
                .reason("Some valid reason")
                .build();
        }
    }

    public abstract ClaimantResponse build();
}

package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;

import java.math.BigDecimal;

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
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithClaimantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueCCJWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(CCJ)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithDefendantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithClaimantPaymentIntention() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(bySetDate())
                .build();
        }

        public ClaimantResponse buildAcceptationIssueSettlementWithCourtDetermination() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(SETTLEMENT)
                .claimantPaymentIntention(bySetDate())
                .courtDetermination(SampleCourtDetermination.bySetDate())
                .build();
        }
    }

    public static class ClaimantResponseRejection extends SampleClaimantResponse<ClaimantResponseAcceptation> {

        private BigDecimal amountPaid = BigDecimal.TEN;
        private boolean freeMediation = false;
        private String reason = "He paid 10 but he actually owes 10,000. No I do not accept this.";

        public ClaimantResponseRejection withAmountPaid(BigDecimal amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        public ClaimantResponseRejection withFreeMediation(boolean freeMediation) {
            this.freeMediation = freeMediation;
            return this;
        }

        public ClaimantResponseRejection withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public static ClaimantResponseRejection builder() {
            return new ClaimantResponseRejection();
        }

        @Override
        public ClaimantResponse build() {
            return ResponseRejection.builder()
                .amountPaid(amountPaid)
                .freeMediation(freeMediation)
                .reason(reason)
                .build();
        }
    }

    public abstract ClaimantResponse build();
}

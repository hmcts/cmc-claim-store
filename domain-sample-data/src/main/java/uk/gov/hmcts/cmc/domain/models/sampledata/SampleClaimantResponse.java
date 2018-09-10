package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.REJECT_PLAN_GO_TO_JUDGE;

public abstract class SampleClaimantResponse<T extends SampleClaimantResponse<T>> {

    public static ClaimantResponse validDefaultAcceptation() {
        return ClaimantResponseAcceptation.builder().build();
    }

    public static ClaimantResponse validDefaultRejection() {
        return ClaimantResponseRejection.builder().build();
    }

    public static class ClaimantResponseAcceptation extends SampleClaimantResponse<ClaimantResponseAcceptation> {

        private BigDecimal amountPaid = BigDecimal.TEN;

        public static ClaimantResponseAcceptation builder() {
            return new ClaimantResponseAcceptation();
        }

        public ClaimantResponseAcceptation withAmountPaid(BigDecimal amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        @Override
        public ClaimantResponse build() {
            return ResponseAcceptation.builder()
                .amountPaid(amountPaid)
                .formaliseOption(REJECT_PLAN_GO_TO_JUDGE)
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

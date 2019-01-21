package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.math.BigDecimal;

public class SampleAmountRow {

    private BigDecimal amount = new BigDecimal("40");
    private String collectionId;
    private String reason = "reason";

    public static SampleAmountRow builder() {
        return new SampleAmountRow();
    }

    public SampleAmountRow withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public SampleAmountRow withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public SampleAmountRow withCollectionId(String collectionId) {
        this.collectionId = collectionId;
        return this;
    }

    public AmountRow build() {
        return new AmountRow(collectionId, reason, amount);
    }
}

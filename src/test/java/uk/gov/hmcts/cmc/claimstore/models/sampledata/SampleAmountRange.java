package uk.gov.hmcts.cmc.claimstore.models.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.amount.AmountRange;

import java.math.BigDecimal;

public class SampleAmountRange {

    private BigDecimal lowerValue = BigDecimal.valueOf(100L);
    private BigDecimal higherValue = BigDecimal.valueOf(99000L);

    public SampleAmountRange withLowerValue(BigDecimal lowerValue) {
        this.lowerValue = lowerValue;
        return this;
    }

    public SampleAmountRange withHigherValue(BigDecimal higherValue) {
        this.higherValue = higherValue;
        return this;
    }

    public static SampleAmountRange builder() {
        return new SampleAmountRange();
    }

    public AmountRange build() {
        return new AmountRange(lowerValue, higherValue);
    }

    public static AmountRange validDefaults() {
        return builder().build();
    }
}

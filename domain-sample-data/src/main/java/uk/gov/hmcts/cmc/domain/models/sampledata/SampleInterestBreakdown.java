package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.math.BigDecimal;

public class SampleInterestBreakdown {

    private BigDecimal totalAmount = new BigDecimal("40.00");
    private String explanation = "I've given this amount because...";

    public static InterestBreakdown validDefaults() {
        return builder().build();
    }

    public static SampleInterestBreakdown builder() {
        return new SampleInterestBreakdown();
    }

    public InterestBreakdown build() {
        return new InterestBreakdown(totalAmount, explanation);
    }

    public SampleInterestBreakdown withTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public SampleInterestBreakdown withExplanation(String explanation) {
        this.explanation = explanation;
        return this;
    }

}

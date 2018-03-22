package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.math.BigDecimal;

public class SampleInterestBreakdown {

    private BigDecimal totalAmount = BigDecimal.valueOf(1065.50);
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

}

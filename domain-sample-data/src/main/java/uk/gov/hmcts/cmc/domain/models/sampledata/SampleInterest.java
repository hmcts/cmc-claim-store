package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.math.BigDecimal;

public class SampleInterest {

    private Interest.InterestType type = Interest.InterestType.DIFFERENT;
    private InterestBreakdown interestBreakdown = null;
    private BigDecimal rate = new BigDecimal(11);
    private String reason = "A reason";
    private BigDecimal specificDailyAmount = null;

    public static SampleInterest builder() {
        return new SampleInterest();
    }

    public static SampleInterest breakdownInterestBuilder() {
        return new SampleInterest()
            .withType(Interest.InterestType.BREAKDOWN)
            .withRate(null)
            .withReason(null);
    }

    public static Interest standard() {
        return builder()
            .withType(Interest.InterestType.STANDARD)
            .withRate(new BigDecimal("8"))
            .withReason(null)
            .build();
    }

    public static Interest noInterest() {
        return builder()
            .withType(Interest.InterestType.NO_INTEREST)
            .withRate(null)
            .withReason(null)
            .build();
    }

    public SampleInterest withType(Interest.InterestType type) {
        this.type = type;
        return this;
    }

    public SampleInterest withInterestBreakdown(InterestBreakdown interestBreakdown) {
        this.interestBreakdown = interestBreakdown;
        return this;
    }

    public SampleInterest withRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    public SampleInterest withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Interest build() {
        return new Interest(
            type,
            interestBreakdown,
            rate,
            reason,
            specificDailyAmount
        );
    }

}

package uk.gov.hmcts.cmccase.models.sampledata;

import uk.gov.hmcts.cmccase.models.Interest;

import java.math.BigDecimal;

public class SampleInterest {

    private Interest.InterestType type = Interest.InterestType.DIFFERENT;
    private BigDecimal rate = new BigDecimal(11);
    private String reason = "A reason";

    public static SampleInterest builder() {
        return new SampleInterest();
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

    public SampleInterest withRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    public SampleInterest withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Interest build() {
        return new Interest(type, rate, reason);
    }

}

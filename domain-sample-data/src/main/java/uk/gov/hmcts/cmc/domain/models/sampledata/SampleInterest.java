package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.math.BigDecimal;

public class SampleInterest {

    private Interest.InterestType type = Interest.InterestType.DIFFERENT;
    private InterestBreakdown interestBreakdown = null;
    private BigDecimal rate = new BigDecimal(11);
    private String reason = "A reason";
    private BigDecimal specificDailyAmount = null;
    private InterestDate interestDate = SampleInterestDate.validDefaults();

    public static SampleInterest builder() {
        return new SampleInterest();
    }

    public static SampleInterest breakdownInterestBuilder() {
        return new SampleInterest()
                .withType(Interest.InterestType.BREAKDOWN)
                .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                .withRate(null)
                .withReason(null);
    }

    public static SampleInterest standardInterestBuilder() {
        return builder()
                .withType(Interest.InterestType.STANDARD)
                .withRate(new BigDecimal("8"))
                .withReason(null);
//                .withInterestDate(SampleInterestDate.builder()
//                    .withDate(LocalDate.of(2015, 02, 02)).build());
    }

    public static SampleInterest noInterestBuilder() {
        return builder()
                .withType(Interest.InterestType.NO_INTEREST)
                .withRate(null)
                .withReason(null);
    }

    public static Interest standard() {
        return standardInterestBuilder().build();
    }

    public static Interest noInterest() {
        return noInterestBuilder().build();
    }

    public static Interest breakdownOnly() {
        return breakdownInterestBuilder().build();
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

    public SampleInterest withSpecificDailyAmount(BigDecimal specificDailyAmount) {
        this.specificDailyAmount = specificDailyAmount;
        return this;
    }

    public SampleInterest withInterestDate(InterestDate interestDate) {
        this.interestDate = interestDate;
        return this;
    }

    public Interest build() {
        return new Interest(
                type,
                interestBreakdown,
                rate,
                reason,
                specificDailyAmount,
                interestDate
        );
    }

}

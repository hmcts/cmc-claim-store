package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.ValidInterest;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ValidInterest
public class Interest {
    public enum InterestType {
        @JsonProperty("standard")
        STANDARD,
        @JsonProperty("different")
        DIFFERENT,
        @JsonProperty("breakdown")
        BREAKDOWN,
        @JsonProperty("no interest")
        NO_INTEREST
    }

    @NotNull
    private final InterestType type;

    private final InterestBreakdown interestBreakdown;

    private final BigDecimal rate;

    private final String reason;

    private final BigDecimal specificDailyAmount;

    private final InterestDate interestDate;

    public Interest(
        InterestType type,
        InterestBreakdown interestBreakdown,
        BigDecimal rate,
        String reason,
        BigDecimal specificDailyAmount,
        InterestDate interestDate
    ) {
        this.type = type;
        this.interestBreakdown = interestBreakdown;
        this.rate = rate;
        this.reason = reason;
        this.specificDailyAmount = specificDailyAmount;
        this.interestDate = interestDate;
    }

    public InterestType getType() {
        return type;
    }

    public InterestBreakdown getInterestBreakdown() {
        return interestBreakdown;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getReason() {
        return reason;
    }

    public Optional<BigDecimal> getSpecificDailyAmount() {
        return Optional.ofNullable(specificDailyAmount);
    }

    public InterestDate getInterestDate() {
        return interestDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Interest interest = (Interest) o;
        return Objects.equals(type, interest.type)
                && Objects.equals(interestBreakdown, interest.interestBreakdown)
                && Objects.equals(rate, interest.rate)
                && Objects.equals(reason, interest.reason)
                && Objects.equals(specificDailyAmount, interest.specificDailyAmount)
                && Objects.equals(interestDate, interest.interestDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, interestBreakdown, rate, reason, specificDailyAmount, interestDate);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

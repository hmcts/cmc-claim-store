package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.InterDependentFields;
import uk.gov.hmcts.cmc.domain.constraints.ValidInterest;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@ValidInterest
@InterDependentFields(field = "rate", dependentField = "type")
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

    public Interest(
        InterestType type,
        InterestBreakdown interestBreakdown,
        BigDecimal rate,
        String reason,
        BigDecimal specificDailyAmount
    ) {
        this.type = type;
        this.interestBreakdown = interestBreakdown;
        this.rate = rate;
        this.reason = reason;
        this.specificDailyAmount = specificDailyAmount;
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

    public BigDecimal getSpecificDailyAmount() {
        return specificDailyAmount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Interest interest = (Interest) other;
        return Objects.equals(type, interest.type)
            && Objects.equals(interestBreakdown, interest.interestBreakdown)
            && Objects.equals(rate, interest.rate)
            && Objects.equals(reason, interest.reason)
            && Objects.equals(specificDailyAmount, interest.specificDailyAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, interestBreakdown, rate, reason, specificDailyAmount);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

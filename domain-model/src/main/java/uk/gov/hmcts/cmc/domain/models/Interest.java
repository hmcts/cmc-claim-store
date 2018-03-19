package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.InterDependentFields;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@InterDependentFields(field = "rate", dependentField = "type")
public class Interest {
    public enum InterestType {
        @JsonProperty("standard")
        STANDARD,
        @JsonProperty("different")
        DIFFERENT,
        @JsonProperty("no interest")
        NO_INTEREST
    }

    public enum InterestOption {
        @JsonProperty("breakdown")
        BREAKDOWN,
        @JsonProperty("same")
        SAME_RATE
    }

    @NotNull
    private final InterestType type;

    private final BigDecimal rate;

    private final String reason;

    private final InterestOption option;

    public Interest(InterestType type,
                    BigDecimal rate,
                    String reason,
                    InterestOption option) {
        this.type = type;
        this.rate = rate;
        this.reason = reason;
        this.option = option;
    }

    public String getReason() {
        return reason;
    }

    public InterestType getType() {
        return type;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public InterestOption getOption() { return option; }

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
            && Objects.equals(rate, interest.rate)
            && Objects.equals(reason, interest.reason)
            && Objects.equals(option, interest.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rate, reason, option);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.constraints.InterDependentFields;
import uk.gov.hmcts.cmc.domain.utils.ToStringStyle;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    @NotNull
    private final InterestType type;

    private final BigDecimal rate;

    private final String reason;

    public Interest(final InterestType type,
                    final BigDecimal rate,
                    final String reason) {
        this.type = type;
        this.rate = rate;
        this.reason = reason;
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
            && Objects.equals(reason, interest.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rate, reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.ourStyle());
    }
}

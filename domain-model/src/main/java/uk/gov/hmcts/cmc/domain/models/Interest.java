package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.ValidInterest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
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

    //This field will only be populated when Case-worker calls 'Recalculate Interest/Claim Fee' Event from CCD
    private final LocalDateTime lastInterestCalculationDate;

    public Interest(
        InterestType type,
        InterestBreakdown interestBreakdown,
        BigDecimal rate,
        String reason,
        BigDecimal specificDailyAmount,
        InterestDate interestDate,
        LocalDateTime lastInterestCalculationDate
    ) {
        this.type = type;
        this.interestBreakdown = interestBreakdown;
        this.rate = rate;
        this.reason = reason;
        this.specificDailyAmount = specificDailyAmount;
        this.interestDate = interestDate;
        this.lastInterestCalculationDate = lastInterestCalculationDate;
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

    public LocalDateTime getLastInterestCalculationDate() {
        return lastInterestCalculationDate;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

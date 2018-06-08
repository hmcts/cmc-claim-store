package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Unemployed {
    
    @NotNull
    private final Integer numberOfYears;
    @NotNull
    private final Integer numberOfMonths;

    public Unemployed(Integer numberOfYears, Integer numberOfMonths) {
        this.numberOfYears = numberOfYears;
        this.numberOfMonths = numberOfMonths;
    }

    public Integer getNumberOfYears() {
        return numberOfYears;
    }

    public Integer getNumberOfMonths() {
        return numberOfMonths;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Unemployed that = (Unemployed) other;
        return Objects.equals(numberOfYears, that.numberOfYears) && Objects.equals(numberOfMonths, that.numberOfMonths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfYears, numberOfMonths);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

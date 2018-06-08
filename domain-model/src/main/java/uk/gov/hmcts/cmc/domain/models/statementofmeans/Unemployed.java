package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Unemployed {
    
    @NotNull
    private final Integer noOfYears;
    @NotNull
    private final Integer noOfMonths;

    public Unemployed(Integer noOfYears, Integer noOfMonths) {
        this.noOfYears = noOfYears;
        this.noOfMonths = noOfMonths;
    }

    public Integer getNoOfYears() {
        return noOfYears;
    }

    public Integer getNoOfMonths() {
        return noOfMonths;
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
        return Objects.equals(noOfYears, that.noOfYears) && Objects.equals(noOfMonths, that.noOfMonths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noOfYears, noOfMonths);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

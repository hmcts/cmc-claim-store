package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

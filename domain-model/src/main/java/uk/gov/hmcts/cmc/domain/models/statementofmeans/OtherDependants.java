package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class OtherDependants {

    @NotNull
    @Min(1)
    private final Integer numberOfPeople;

    @NotBlank
    private final String details;

    private final boolean anyDisabled;

    public OtherDependants(Integer numberOfPeople, String details, boolean anyDisabled) {
        this.numberOfPeople = numberOfPeople;
        this.details = details;
        this.anyDisabled = anyDisabled;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public String getDetails() {
        return details;
    }

    public boolean areAnyDisabled() {
        return this.anyDisabled;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

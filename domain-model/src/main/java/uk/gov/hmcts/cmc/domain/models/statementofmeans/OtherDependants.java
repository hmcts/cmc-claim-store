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

    public OtherDependants(Integer numberOfPeople, String details) {
        this.numberOfPeople = numberOfPeople;
        this.details = details;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

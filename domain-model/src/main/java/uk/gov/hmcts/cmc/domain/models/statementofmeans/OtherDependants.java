package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        OtherDependants that = (OtherDependants) other;
        return Objects.equals(numberOfPeople, that.numberOfPeople)
            && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfPeople, details);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

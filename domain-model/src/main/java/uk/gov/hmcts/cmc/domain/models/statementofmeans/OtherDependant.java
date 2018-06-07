package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class OtherDependant {

    @NotNull
    @Min(1)
    private final Integer noOfPeople;

    @NotEmpty
    private final String details;

    public OtherDependant(Integer noOfPeople, String details) {
        this.noOfPeople = noOfPeople;
        this.details = details;
    }

    public Integer getNoOfPeople() {
        return noOfPeople;
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
        OtherDependant that = (OtherDependant) other;
        return Objects.equals(noOfPeople, that.noOfPeople)
            && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noOfPeople, details);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

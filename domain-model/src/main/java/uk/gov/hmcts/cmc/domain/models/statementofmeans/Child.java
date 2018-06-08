package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Child {

    public enum AgeGroupType {
        UNDER_11,
        BETWEEN_11_AND_15,
        BETWEEN_16_AND_19;
    }

    @NotNull
    private final AgeGroupType ageGroupType;

    @NotNull
    private final Integer howMany;

    public Child(AgeGroupType ageGroupType, Integer howMany) {
        this.ageGroupType = ageGroupType;
        this.howMany = howMany;
    }

    public AgeGroupType getAgeGroupType() {
        return ageGroupType;
    }

    public Integer getHowMany() {
        return howMany;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Child child = (Child) other;
        return ageGroupType == child.ageGroupType && Objects.equals(howMany, child.howMany);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ageGroupType, howMany);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

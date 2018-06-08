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
    private final Integer numberOfChildren;

    public Child(AgeGroupType ageGroupType, Integer numberOfChildren) {
        this.ageGroupType = ageGroupType;
        this.numberOfChildren = numberOfChildren;
    }

    public AgeGroupType getAgeGroupType() {
        return ageGroupType;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
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
        return ageGroupType == child.ageGroupType && Objects.equals(numberOfChildren, child.numberOfChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ageGroupType, numberOfChildren);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

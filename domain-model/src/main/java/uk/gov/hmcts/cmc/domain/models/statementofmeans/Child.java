package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;
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

    private final Integer numberOfChildrenLivingWithYou;

    public Child(
        AgeGroupType ageGroupType,
        Integer numberOfChildren,
        Integer numberOfChildrenLivingWithYou
    ) {
        this.ageGroupType = ageGroupType;
        this.numberOfChildren = numberOfChildren;
        this.numberOfChildrenLivingWithYou = numberOfChildrenLivingWithYou;
    }

    public AgeGroupType getAgeGroupType() {
        return ageGroupType;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public Optional<Integer> getNumberOfChildrenLivingWithYou() {
        return Optional.ofNullable(numberOfChildrenLivingWithYou);
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
        return ageGroupType == child.ageGroupType
            && Objects.equals(numberOfChildren, child.numberOfChildren)
            && Objects.equals(numberOfChildrenLivingWithYou, child.numberOfChildrenLivingWithYou);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ageGroupType, numberOfChildren, numberOfChildrenLivingWithYou);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.util.Optional;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class Child extends CollectionId {

    public enum AgeGroupType {
        UNDER_11("Under 11"),
        BETWEEN_11_AND_15("Between 11 and 15"),
        BETWEEN_16_AND_19("Between 16 and 19");

        private final String description;

        AgeGroupType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    @NotNull
    private final AgeGroupType ageGroupType;

    @Min(0)
    @NotNull
    private final Integer numberOfChildren;

    @Min(0)
    private final Integer numberOfChildrenLivingWithYou;

    @Builder
    public Child(
        String id,
        AgeGroupType ageGroupType,
        Integer numberOfChildren,
        Integer numberOfChildrenLivingWithYou
    ) {
        super(id);
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

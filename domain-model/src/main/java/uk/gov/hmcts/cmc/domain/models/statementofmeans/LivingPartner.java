package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class LivingPartner {
    public enum AgeGroupType {
        CHILD("Under 18"),
        YOUNG_ADULT("Young adult, under 25"),
        ADULT("Adult, over 25"),
        PENSIONER("Pensioner");

        String description;

        AgeGroupType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private final boolean declared;
    private final AgeGroupType ageGroup;
    private final DisabilityStatus disability;

    public LivingPartner(boolean declared, AgeGroupType ageGroup, DisabilityStatus disability) {
        this.declared = declared;
        this.ageGroup = ageGroup;
        this.disability = disability;
    }

    public boolean isDeclared() {
        return declared;
    }

    public AgeGroupType getAgeGroup() {
        return ageGroup;
    }

    public DisabilityStatus getDisability() {
        return disability;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

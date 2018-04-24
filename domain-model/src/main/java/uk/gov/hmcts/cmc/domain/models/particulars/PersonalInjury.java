package uk.gov.hmcts.cmc.domain.models.particulars;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class PersonalInjury {

    @NotNull
    private final DamagesExpectation generalDamages;

    @JsonCreator
    public PersonalInjury(DamagesExpectation generalDamages) {
        this.generalDamages = generalDamages;
    }

    public DamagesExpectation getGeneralDamages() {
        return generalDamages;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PersonalInjury that = (PersonalInjury) obj;

        return Objects.equals(this.generalDamages, that.generalDamages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generalDamages);
    }

}

package uk.gov.hmcts.cmccase.models.particulars;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class HousingDisrepair {

    @NotNull
    private final DamagesExpectation costOfRepairsDamages;

    private final DamagesExpectation otherDamages;

    public HousingDisrepair(DamagesExpectation costOfRepairsDamages, DamagesExpectation otherDamages) {
        this.costOfRepairsDamages = costOfRepairsDamages;
        this.otherDamages = otherDamages;
    }

    public DamagesExpectation getCostOfRepairsDamages() {
        return costOfRepairsDamages;
    }

    public Optional<DamagesExpectation> getOtherDamages() {
        return Optional.ofNullable(otherDamages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        HousingDisrepair that = (HousingDisrepair) obj;

        return Objects.equals(this.costOfRepairsDamages, that.costOfRepairsDamages)
            && Objects.equals(this.otherDamages, that.otherDamages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(costOfRepairsDamages, otherDamages);
    }

}

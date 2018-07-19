package uk.gov.hmcts.cmc.domain.models.particulars;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
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
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class HousingDisrepair {
    private final DamagesExpectation costOfRepairsDamages;
    private final DamagesExpectation otherDamages;

}

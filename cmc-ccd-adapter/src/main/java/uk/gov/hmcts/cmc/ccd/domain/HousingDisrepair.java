package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HousingDisrepair {
    private final DamagesExpectation costOfRepairsDamages;
    private final DamagesExpectation otherDamages;

}

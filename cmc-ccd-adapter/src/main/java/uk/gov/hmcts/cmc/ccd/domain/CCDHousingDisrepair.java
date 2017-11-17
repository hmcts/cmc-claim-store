package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDHousingDisrepair {
    private final CCDDamagesExpectation costOfRepairsDamages;
    private final CCDDamagesExpectation otherDamages;

}

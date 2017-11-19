package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDHousingDisrepair {
    private final String costOfRepairsDamages;
    private final String otherDamages;

}

package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDHousingDisrepair {
    private String costOfRepairsDamages;
    private String otherDamages;

}

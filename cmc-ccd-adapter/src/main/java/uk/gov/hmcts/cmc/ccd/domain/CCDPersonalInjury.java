package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDPersonalInjury {
    private final CCDDamagesExpectation generalDamages;
}

package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalInjury {
    private final DamagesExpectation generalDamages;
}

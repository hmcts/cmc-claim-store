package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPersonalInjury {
    private String generalDamages;

    @JsonCreator
    public CCDPersonalInjury(String generalDamages) {
        this.generalDamages = generalDamages;
    }
}

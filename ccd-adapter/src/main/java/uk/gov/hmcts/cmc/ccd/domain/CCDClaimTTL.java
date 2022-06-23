package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
public class CCDClaimTTL {
    private LocalDate SystemTTL;
    private LocalDate OverrideTTL;
    private CCDYesNoOption Suspended;

}

package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
public class ClaimTTL {
    @JsonProperty("SystemTTL")
    private LocalDate systemTTL;
    @JsonProperty("OverrideTTL")
    private LocalDate overrideTTL;
    @JsonProperty("Suspended")
    private YesNoOption suspended;
}

package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingCourt {
    String name;
    CCDAddress address;
}

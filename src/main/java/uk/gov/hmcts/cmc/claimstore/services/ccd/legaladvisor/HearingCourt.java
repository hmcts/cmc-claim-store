package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;

@Builder
@Value
public class HearingCourt {
    String name;
    String address;
}

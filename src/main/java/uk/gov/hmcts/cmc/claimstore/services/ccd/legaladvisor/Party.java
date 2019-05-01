package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Party {
    private String partyName;
}

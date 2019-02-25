package uk.gov.hmcts.cmc.claimstore.events.response;

import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Value
public class DefendantResponseEvent {
    Claim claim;
    String authorization;

    public DefendantResponseEvent(Claim claim, String authorization) {
        this.claim = claim;
        this.authorization = authorization;
    }
}

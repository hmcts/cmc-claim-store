package uk.gov.hmcts.cmc.claimstore.events.response;

import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Value
public class DefendantPaperResponseEvent {
    private final Claim claim;
    private final String authorization;

    public DefendantPaperResponseEvent(Claim claim, String authorization) {
        this.claim = claim;
        this.authorization = authorization;
    }
}

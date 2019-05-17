package uk.gov.hmcts.cmc.claimstore.events;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Builder
@Getter
public class ClaimCreationEvent {
    public Claim claim;
    public String submitterName;
    public String authorisation;

    public ClaimCreationEvent(Claim claim, String submitterName, String authorisation) {
        this.claim = claim;
        this.submitterName = submitterName;
        this.authorisation = authorisation;
    }
}

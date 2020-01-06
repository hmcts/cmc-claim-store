package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class ResendPinEvent {
    protected final Claim claim;
    protected final String authorisation;

    public ResendPinEvent(Claim claim, String authorisation) {
        this.claim = claim;
        this.authorisation = authorisation;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

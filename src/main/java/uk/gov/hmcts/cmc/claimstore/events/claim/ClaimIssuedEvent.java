package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ClaimIssuedEvent {
    protected final Claim claim;
    protected final String submitterName;
    protected final String authorisation;

    public ClaimIssuedEvent(
        Claim claim,
        String submitterName,
        String authorisation
    ) {
        this.claim = claim;
        this.submitterName = submitterName;
        this.authorisation = authorisation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

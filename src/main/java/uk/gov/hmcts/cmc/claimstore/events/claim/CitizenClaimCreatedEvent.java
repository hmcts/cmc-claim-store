package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class CitizenClaimCreatedEvent {
    private final Claim claim;
    private final String submitterName;
    private final String authorisation;

    public CitizenClaimCreatedEvent(
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

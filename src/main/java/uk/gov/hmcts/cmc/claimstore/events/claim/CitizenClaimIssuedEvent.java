package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CitizenClaimIssuedEvent extends ClaimIssuedEvent {
    private final String pin;

    public CitizenClaimIssuedEvent(
        Claim claim,
        String pin,
        String submitterName,
        String authorisation
    ) {
        super(claim, submitterName, authorisation);
        this.pin = pin;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

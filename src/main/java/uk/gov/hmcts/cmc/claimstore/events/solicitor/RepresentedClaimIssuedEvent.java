package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class RepresentedClaimIssuedEvent extends ClaimIssuedEvent {

    public RepresentedClaimIssuedEvent(Claim claim, String submitterName, String authorisation) {
        super(claim, submitterName, authorisation);
    }

    public String getRepresentativeEmail() {
        return getClaim().getSubmitterEmail();
    }

    public Optional<String> getRepresentativeName() {
        return Optional.ofNullable(getSubmitterName());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

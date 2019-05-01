package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.events.ClaimCreationEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = false)
@Value
public class RepresentedClaimCreatedEvent extends ClaimCreationEvent {

    public RepresentedClaimCreatedEvent(Claim claim, String submitterName, String authorisation) {
        super(claim, submitterName, authorisation);
    }

    public String getRepresentativeEmail() {
        return this.claim.getSubmitterEmail();
    }

    public Optional<String> getRepresentativeName() {
        return Optional.ofNullable(this.submitterName);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

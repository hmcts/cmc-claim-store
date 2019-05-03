package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@Builder(toBuilder = true)
public class RepresentedClaimCreatedEvent {

    private final Claim claim;
    private final String submitterName;
    private final String authorisation;

    public RepresentedClaimCreatedEvent(Claim claim, String submitterName, String authorisation) {
        this.claim = claim;
        this.submitterName = submitterName;
        this.authorisation = authorisation;
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

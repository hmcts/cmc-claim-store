package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class RepresentedClaimIssuedEvent {

    private final Claim claim;
    private final String representativeName;
    private final String representativeEmail;
    private final String authorisation;

    public RepresentedClaimIssuedEvent(Claim claim, String submitterName, String authorisation) {
        this.claim = claim;
        this.representativeName = submitterName;
        this.representativeEmail = claim.getSubmitterEmail();
        this.authorisation = authorisation;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getRepresentativeEmail() {
        return representativeEmail;
    }

    public Optional<String> getRepresentativeName() {
        return Optional.ofNullable(representativeName);
    }

    public String getAuthorisation() {
        return authorisation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        RepresentedClaimIssuedEvent that = (RepresentedClaimIssuedEvent) obj;

        return Objects.equals(claim, that.claim)
            && Objects.equals(representativeName, that.representativeName)
            && Objects.equals(representativeEmail, that.representativeEmail)
            && Objects.equals(authorisation, that.authorisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, representativeName, representativeEmail, authorisation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

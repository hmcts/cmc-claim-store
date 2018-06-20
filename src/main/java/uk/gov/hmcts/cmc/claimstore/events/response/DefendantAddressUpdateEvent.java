package uk.gov.hmcts.cmc.claimstore.events.response;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;

public class DefendantAddressUpdateEvent {
    private final Claim claim;
    private final TheirDetails defendant;

    public DefendantAddressUpdateEvent(Claim claim, TheirDetails defendant) {
        this.claim = claim;
        this.defendant = defendant;
    }

    public Claim getClaim() { return claim; }
    public TheirDetails getDefendant() { return defendant; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DefendantAddressUpdateEvent that = (DefendantAddressUpdateEvent) obj;
        return Objects.equals(claim, that.getClaim())
            && Objects.equals(defendant, that.getDefendant());
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, defendant);
    }
}

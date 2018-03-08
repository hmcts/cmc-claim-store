package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class CitizenClaimIssuedEvent extends ClaimIssuedEvent {
    private final String pin;

    public CitizenClaimIssuedEvent(Claim claim,
                                   String pin,
                                   String submitterName,
                                   String authorisation
    ) {
        super(claim, submitterName, authorisation);
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        CitizenClaimIssuedEvent that = (CitizenClaimIssuedEvent) other;
        return Objects.equals(pin, that.pin);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), pin);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

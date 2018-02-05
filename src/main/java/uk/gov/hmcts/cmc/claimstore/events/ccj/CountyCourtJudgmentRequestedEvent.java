package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class CountyCourtJudgmentRequestedEvent {

    private final Claim claim;
    private final String authorisation;

    public CountyCourtJudgmentRequestedEvent(Claim claim, String authorisation) {
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CountyCourtJudgmentRequestedEvent that = (CountyCourtJudgmentRequestedEvent) other;
        return Objects.equals(claim, that.claim)
            && Objects.equals(authorisation, that.authorisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim, authorisation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

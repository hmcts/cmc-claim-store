package uk.gov.hmcts.cmc.claimstore.events.ccj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ReDeterminationEvent {
    private final Claim claim;
    private final String authorisation;
    private final String submitterName;
    private final MadeBy partyType;

    public ReDeterminationEvent(Claim claim, String authorisation, String submitterName, MadeBy partyType) {
        this.claim = claim;
        this.authorisation = authorisation;
        this.submitterName = submitterName;
        this.partyType = partyType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}

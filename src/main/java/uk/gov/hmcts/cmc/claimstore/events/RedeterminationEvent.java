package uk.gov.hmcts.cmc.claimstore.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class RedeterminationEvent {
    private final Claim claim;
    private final String authorisation;
    private final String submitterName;

    public RedeterminationEvent(Claim claim, String authorisation, String submitterName) {
        this.claim = claim;
        this.authorisation = authorisation;
        this.submitterName = submitterName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}

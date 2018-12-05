package uk.gov.hmcts.cmc.claimstore.events.ccj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class CountyCourtJudgmentEvent {

    private final Claim claim;
    private final String authorisation;

    public CountyCourtJudgmentEvent(Claim claim, String authorisation) {
        this.claim = claim;
        this.authorisation = authorisation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

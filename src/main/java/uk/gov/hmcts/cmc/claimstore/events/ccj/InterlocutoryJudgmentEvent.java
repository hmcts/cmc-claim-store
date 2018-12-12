package uk.gov.hmcts.cmc.claimstore.events.ccj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class InterlocutoryJudgmentEvent {

    private final Claim claim;

    public InterlocutoryJudgmentEvent(Claim claim) {
        this.claim = claim;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

package uk.gov.hmcts.cmc.claimstore.events.ccj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class InterlocutoryJudgmentEvent {

    private final Claim claim;
    private final ResponseAcceptation responseAcceptation;

    public InterlocutoryJudgmentEvent(Claim claim, ResponseAcceptation responseAcceptation) {
        this.claim = claim;
        this.responseAcceptation = responseAcceptation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

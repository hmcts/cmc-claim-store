package uk.gov.hmcts.cmc.claimstore.events.ccj;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class CountyCourtJudgmentEvent {

    private final Claim claim;
    private final String authorisation;
    private final boolean issue;

    public CountyCourtJudgmentEvent(Claim claim, String authorisation, boolean issue) {
        this.claim = claim;
        this.authorisation = authorisation;
        this.issue = issue;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    public boolean isIssue() {
        return issue;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

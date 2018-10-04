package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDCountyCourtJudgmentEvent {
    private final String authorization;
    private final Claim claim;
    private final CountyCourtJudgment countyCourtJudgment;
    private final boolean issue;

    public CCDCountyCourtJudgmentEvent(
        String authorization,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    ) {
        this.authorization = authorization;
        this.claim = claim;
        this.countyCourtJudgment = countyCourtJudgment;
        this.issue = issue;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

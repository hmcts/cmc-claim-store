package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class CCDSettlementEvent {
    private final Claim claim;
    private final Settlement settlement;
    private final String authorization;
    private final CaseEvent caseEvent;

    public CCDSettlementEvent(
        Claim claim,
        Settlement settlement,
        String authorization,
        CaseEvent caseEvent
    ) {

        this.claim = claim;
        this.settlement = settlement;
        this.authorization = authorization;
        this.caseEvent = caseEvent;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

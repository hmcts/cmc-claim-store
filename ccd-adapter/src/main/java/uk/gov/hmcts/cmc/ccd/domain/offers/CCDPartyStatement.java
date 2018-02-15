package uk.gov.hmcts.cmc.ccd.domain.offers;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPartyStatement {
    private CCDStatementType type;
    private CCDMadeBy madeBy;
    private CCDOffer offer;
}

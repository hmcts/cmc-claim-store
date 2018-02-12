package uk.gov.hmcts.cmc.ccd.domain.offers;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDPartyStatementArrayElement {
    private String id;
    private CCDPartyStatement value;
}

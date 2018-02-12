package uk.gov.hmcts.cmc.ccd.domain.offers;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CCDSettlement {
    private List<CCDPartyStatementArrayElement> partyStatements;
}

package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;

@Value
@Builder
public class CCDResponse {
    CCDDefenceType defenceType;
    String defence;
    String freeMediation;
    String moreTimeNeeded;
    CCDParty defendant;
    CCDStatementOfTruth statementOfTruth;
}

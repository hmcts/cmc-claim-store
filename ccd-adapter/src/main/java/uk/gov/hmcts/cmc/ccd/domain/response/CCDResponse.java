package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Value
@Builder
public class CCDResponse {
    CCDDefenceType responseType;
    String defence;
    CCDYesNoOption freeMediationOption;
    CCDYesNoOption moreTimeNeededOption;
    CCDParty defendant;
    CCDStatementOfTruth statementOfTruth;
}

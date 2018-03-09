package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Value
@Builder
public class CCDResponse {
    private CCDDefenceType responseType;
    private String defence;
    private CCDPaymentDeclaration paymentDeclaration;
    private CCDYesNoOption freeMediationOption;
    private CCDYesNoOption moreTimeNeededOption;
    private CCDParty defendant;
    private CCDStatementOfTruth statementOfTruth;
}

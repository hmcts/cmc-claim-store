package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDDefendantEvidence;

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
    private CCDDefendantTimeline timeline;
    private CCDDefendantEvidence evidence;
}

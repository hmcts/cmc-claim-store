package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Value
@Builder
public class CCDFullDefenceResponse {
    private CCDDefenceType defenceType;
    private String defence;
    private CCDPaymentDeclaration paymentDeclaration;
    private CCDYesNoOption freeMediationOption;
    private CCDYesNoOption moreTimeNeededOption;
    private CCDDefendant defendant;
    private CCDStatementOfTruth statementOfTruth;
    private CCDDefendantTimeline timeline;
    private CCDDefendantEvidence evidence;
}

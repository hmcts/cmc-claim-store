package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;

@Value
@Builder
public class CCDFullDefenceResponse {
    private CCDDefenceType defenceType;
    private String defence;
    private CCDPaymentDeclaration paymentDeclaration;
    private CCDYesNoOption freeMediationOption;
    private CCDYesNoOption moreTimeNeededOption;
    private CCDParty defendant;
    private CCDStatementOfTruth statementOfTruth;
    private CCDDefendantTimeline timeline;
    private CCDDefendantEvidence evidence;
}

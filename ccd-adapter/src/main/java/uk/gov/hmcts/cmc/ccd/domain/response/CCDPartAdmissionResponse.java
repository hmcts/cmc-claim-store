package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDStatementOfMeans;

import java.math.BigDecimal;

@Builder
@Value
public class CCDPartAdmissionResponse {
    private CCDYesNoOption moreTimeNeededOption;
    private CCDYesNoOption freeMediationOption;
    private CCDParty defendant;
    private BigDecimal amount;
    private CCDPaymentDeclaration paymentDeclaration;
    private CCDPaymentIntention paymentIntention;
    private String defence;
    private CCDDefendantTimeline timeline;
    private CCDDefendantEvidence evidence;
    private CCDStatementOfMeans statementOfMeans;
    private CCDStatementOfTruth statementOfTruth;
}

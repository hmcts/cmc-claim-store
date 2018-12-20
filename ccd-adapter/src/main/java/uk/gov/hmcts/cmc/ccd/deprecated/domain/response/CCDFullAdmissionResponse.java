package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

@Builder
@Value
public class CCDFullAdmissionResponse {
    private CCDYesNoOption moreTimeNeededOption;
    private CCDYesNoOption freeMediationOption;
    private CCDDefendant defendant;
    private CCDPaymentIntention paymentIntention;
    private CCDStatementOfMeans statementOfMeans;
    private CCDStatementOfTruth statementOfTruth;
}

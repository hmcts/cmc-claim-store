package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDStatementOfMeans;

import java.time.LocalDate;

@Builder
@Value
public class CCDFullAdmissionResponse {
    private CCDYesNoOption moreTimeNeededOption;
    private CCDYesNoOption freeMediationOption;
    private CCDParty defendant;
    private CCDPaymentOption paymentOption;
    private LocalDate paymentDate;
    private CCDRepaymentPlan repaymentPlan;
    private CCDStatementOfMeans statementOfMeans;
    private CCDStatementOfTruth statementOfTruth;
}

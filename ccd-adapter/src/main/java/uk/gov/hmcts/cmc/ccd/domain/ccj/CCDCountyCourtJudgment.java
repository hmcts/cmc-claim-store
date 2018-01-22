package uk.gov.hmcts.cmc.ccd.domain.ccj;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;

import java.math.BigDecimal;

@Value
@Builder
public class CCDCountyCourtJudgment {

    private String defendantDateOfBirth;
    private BigDecimal paidAmount;
    private CCDPaymentOption paymentOption;
    private CCDRepaymentPlan repaymentPlan;
    private String payBySetDate;
    private CCDStatementOfTruth statementOfTruth;
}

package uk.gov.hmcts.cmc.ccd.domain.ccj;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CCDCountyCourtJudgment {

    private LocalDate defendantDateOfBirth;
    private BigDecimal paidAmount;
    private CCDPaymentOption paymentOption;
    private CCDRepaymentPlan repaymentPlan;
    private LocalDate payBySetDate;
    private CCDStatementOfTruth statementOfTruth;
}

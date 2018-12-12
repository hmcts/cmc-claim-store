package uk.gov.hmcts.cmc.ccd.deprecated.domain.ccj;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;

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
    private CountyCourtJudgmentType ccjType;
}

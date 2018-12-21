package uk.gov.hmcts.cmc.ccd.domain.ccj;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CCDCountyCourtJudgment {

    private LocalDate defendantDateOfBirth;
    private BigDecimal paidAmount;
    private PaymentOption paymentOption;
    private BigDecimal repaymentPlanInstalmentAmount;
    private LocalDate repaymentPlanFirstPaymentDate;
    private LocalDate repaymentPlanCompletionDate;
    private String repaymentPlanPaymentLength;
    private PaymentSchedule repaymentPlanPaymentSchedule;
    private LocalDate payBySetDate;
    private String statementOfTruthSignerName;
    private String statementOfTruthSignerRole;
    private CountyCourtJudgmentType ccjType;
}

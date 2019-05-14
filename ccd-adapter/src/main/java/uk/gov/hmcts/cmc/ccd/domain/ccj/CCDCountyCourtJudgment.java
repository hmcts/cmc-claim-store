package uk.gov.hmcts.cmc.ccd.domain.ccj;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class CCDCountyCourtJudgment {

    private LocalDateTime requestedDate;
    private LocalDate defendantDateOfBirth;
    private String paidAmount;
    private CCDPaymentOption paymentOption;
    private String repaymentPlanInstalmentAmount;
    private LocalDate repaymentPlanFirstPaymentDate;
    private LocalDate repaymentPlanCompletionDate;
    private String repaymentPlanPaymentLength;
    private CCDPaymentSchedule repaymentPlanPaymentSchedule;
    private LocalDate payBySetDate;
    private String statementOfTruthSignerName;
    private String statementOfTruthSignerRole;
    private CCDCountyCourtJudgmentType type;
}

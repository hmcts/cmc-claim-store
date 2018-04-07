package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public final class RepaymentPlanContentProvider {

    private RepaymentPlanContentProvider() {
        // Utils class no constructing
    }

    public static RepaymentPlanContent create(CountyCourtJudgment countyCourtJudgment) {
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                return new RepaymentPlanContent(PaymentOption.IMMEDIATELY.getDescription());
            case INSTALMENTS:
                RepaymentPlan repaymentPlan = countyCourtJudgment.getRepaymentPlan()
                    .orElseThrow(IllegalArgumentException::new);
                return new RepaymentPlanContent(PaymentOption.INSTALMENTS.getDescription(),
                    formatMoney(repaymentPlan.getInstalmentAmount()),
                    formatDate(repaymentPlan.getFirstPaymentDate()),
                    repaymentPlan.getPaymentSchedule().getDescription());
            case FULL_BY_SPECIFIED_DATE:
                return new RepaymentPlanContent(PaymentOption.FULL_BY_SPECIFIED_DATE.getDescription(),
                    formatDate(countyCourtJudgment.getPayBySetDate()
                        .orElseThrow(IllegalArgumentException::new)));
            default:
                throw new IllegalArgumentException(
                    "Unknown repayment type: " + countyCourtJudgment.getPaymentOption()
                );
        }
    }
}

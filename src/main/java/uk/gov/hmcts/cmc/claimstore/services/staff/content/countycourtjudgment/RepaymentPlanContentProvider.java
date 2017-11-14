package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmccase.models.CountyCourtJudgment;
import uk.gov.hmcts.cmccase.models.ccj.PaymentOption;
import uk.gov.hmcts.cmccase.models.ccj.RepaymentPlan;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public final class RepaymentPlanContentProvider {

    private RepaymentPlanContentProvider() {
        // Utils class no constructing
    }

    public static String create(CountyCourtJudgment countyCourtJudgment) {
        switch (countyCourtJudgment.getPaymentOption()) {
            case IMMEDIATELY:
                return PaymentOption.IMMEDIATELY.getDescription();
            case INSTALMENTS:
                RepaymentPlan repaymentPlan = countyCourtJudgment.getRepaymentPlan()
                    .orElseThrow(IllegalArgumentException::new);
                return getRepaymentPlanContent(repaymentPlan);
            case FULL_BY_SPECIFIED_DATE:
                return String.format(
                    PaymentOption.FULL_BY_SPECIFIED_DATE.getDescription(),
                    formatDate(countyCourtJudgment.getPayBySetDate()
                        .orElseThrow(IllegalArgumentException::new)));
            default:
                throw new IllegalArgumentException(
                    "Unknown repayment type: " + countyCourtJudgment.getPaymentOption()
                );
        }
    }

    private static String getRepaymentPlanContent(RepaymentPlan repaymentPlan) {
        return String.format(
            "first payment of %s on %s. This will be followed by %s %s",
            formatMoney(repaymentPlan.getFirstPayment()),
            formatDate(repaymentPlan.getFirstPaymentDate()),
            formatMoney(repaymentPlan.getInstalmentAmount()),
            repaymentPlan.getPaymentSchedule().getDescription()
        );
    }
}

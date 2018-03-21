package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;

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
                return new RepaymentPlanContent(PaymentOption.INSTALMENTS.getDescription(),
                    formatMoney(countyCourtJudgment.getRepaymentPlan().get().getInstalmentAmount()),
                    formatDate(countyCourtJudgment.getRepaymentPlan().get().getFirstPaymentDate()),
                    countyCourtJudgment.getRepaymentPlan().get().getPaymentSchedule().getDescription());
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

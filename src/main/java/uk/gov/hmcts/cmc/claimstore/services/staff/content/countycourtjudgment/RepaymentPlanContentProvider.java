package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public final class RepaymentPlanContentProvider {
    private CountyCourtJudgment countyCourtJudgment;

    public RepaymentPlanContentProvider(CountyCourtJudgment countyCourtJudgment) {
        this.countyCourtJudgment = countyCourtJudgment;
    }

    public RepaymentPlanContent create() {
        return new RepaymentPlanContent(
            countyCourtJudgment.getPaymentOption().getDescription(),
            formatMoney(countyCourtJudgment.getRepaymentPlan().get().getInstalmentAmount()),
            formatDate(countyCourtJudgment.getRepaymentPlan().get().getFirstPaymentDate()),
            countyCourtJudgment.getRepaymentPlan().get().getPaymentSchedule().getDescription()
        );
    }
}

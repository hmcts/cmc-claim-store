package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public final class RepaymentPlanContentProvider {

    @Autowired
    public RepaymentPlanContentProvider() {
    }

    public RepaymentPlanContent create(CountyCourtJudgment countyCourtJudgment) {
        return new RepaymentPlanContent(
            countyCourtJudgment.getPaymentOption().getDescription(),
            formatMoney(countyCourtJudgment.getRepaymentPlan().get().getInstalmentAmount()),
            formatDate(countyCourtJudgment.getRepaymentPlan().get().getFirstPaymentDate()),
            countyCourtJudgment.getRepaymentPlan().get().getPaymentSchedule().getDescription()
        );
    }
}

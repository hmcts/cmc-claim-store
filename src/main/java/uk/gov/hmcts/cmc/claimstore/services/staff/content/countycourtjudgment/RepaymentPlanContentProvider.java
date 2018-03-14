package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import java.util.Optional;

public final class RepaymentPlanContentProvider {

    private RepaymentPlanContentProvider() {
        // Utils class no constructing
    }

    public static Optional<RepaymentPlan> create(CountyCourtJudgment countyCourtJudgment) {
        return countyCourtJudgment.getRepaymentPlan();
    }
}

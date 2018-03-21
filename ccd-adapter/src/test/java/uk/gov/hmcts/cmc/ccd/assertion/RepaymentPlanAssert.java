package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import java.util.Objects;

public class RepaymentPlanAssert extends AbstractAssert<RepaymentPlanAssert, RepaymentPlan> {

    public RepaymentPlanAssert(RepaymentPlan actual) {
        super(actual, RepaymentPlanAssert.class);
    }

    public RepaymentPlanAssert isEqualTo(CCDRepaymentPlan ccdRepaymentPlan) {
        isNotNull();

        if (!Objects.equals(actual.getInstalmentAmount(), ccdRepaymentPlan.getInstalmentAmount())) {
            failWithMessage("Expected RepaymentPlan.instalmentAmount to be <%s> but was <%s>",
                ccdRepaymentPlan.getInstalmentAmount(), actual.getInstalmentAmount());
        }

        if (!Objects.equals(actual.getFirstPaymentDate(), ccdRepaymentPlan.getFirstPaymentDate())) {
            failWithMessage("Expected RepaymentPlan.firstPaymentDate to be <%s> but was <%s>",
                ccdRepaymentPlan.getFirstPaymentDate(), actual.getFirstPaymentDate());
        }

        if (!Objects.equals(actual.getPaymentSchedule().name(), ccdRepaymentPlan.getPaymentSchedule().name())) {
            failWithMessage("Expected RepaymentPlan.paymentSchedule to be <%s> but was <%s>",
                ccdRepaymentPlan.getPaymentSchedule().name(), actual.getPaymentSchedule().name());
        }

        return this;
    }
}

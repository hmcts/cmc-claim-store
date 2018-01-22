package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;

import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;

public class RepaymentPlanAssert extends AbstractAssert<RepaymentPlanAssert, RepaymentPlan> {

    public RepaymentPlanAssert(RepaymentPlan actual) {
        super(actual, RepaymentPlanAssert.class);
    }

    public RepaymentPlanAssert isEqualTo(CCDRepaymentPlan ccdRepaymentPlan) {
        isNotNull();

        if (!Objects.equals(actual.getFirstPayment(), ccdRepaymentPlan.getFirstPayment())) {
            failWithMessage("Expected RepaymentPlan.firstPayment to be <%s> but was <%s>",
                ccdRepaymentPlan.getFirstPayment(), actual.getFirstPayment());
        }

        if (!Objects.equals(actual.getInstalmentAmount(), ccdRepaymentPlan.getInstalmentAmount())) {
            failWithMessage("Expected RepaymentPlan.instalmentAmount to be <%s> but was <%s>",
                ccdRepaymentPlan.getInstalmentAmount(), actual.getInstalmentAmount());
        }

        if (!Objects.equals(actual.getFirstPaymentDate().format(ISO_DATE),
            ccdRepaymentPlan.getFirstPaymentDate())) {
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

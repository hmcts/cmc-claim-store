package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class PaymentIntentionAssert extends CustomAssert<PaymentIntentionAssert, PaymentIntention> {

    public PaymentIntentionAssert(PaymentIntention actual) {
        super("PaymentIntention", actual, PaymentIntentionAssert.class);
    }

    public PaymentIntentionAssert isEqualTo(CCDPaymentIntention expected) {
        isNotNull();

        compare("paymentOption",
            expected.getPaymentOption(), Enum::name,
            Optional.ofNullable(actual.getPaymentOption()).map(Enum::name));

        compare("instalmentAmount",
            expected.getInstalmentAmount(),
            actual.getRepaymentPlan().map(RepaymentPlan::getInstalmentAmount),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("firstPaymentDate",
            expected.getFirstPaymentDate(),
            actual.getRepaymentPlan().map(RepaymentPlan::getFirstPaymentDate));

        compare("paymentSchedule",
            expected.getPaymentSchedule(), Enum::name,
            actual.getRepaymentPlan().map(RepaymentPlan::getPaymentSchedule).map(Enum::name));

        compare("paymentLength",
            expected.getPaymentLength(),
            actual.getRepaymentPlan().map(RepaymentPlan::getPaymentLength));

        compare("completionDate",
            expected.getCompletionDate(),
            actual.getRepaymentPlan().map(RepaymentPlan::getCompletionDate));

        compare("paymentDate",
            expected.getPaymentDate(),
            actual.getPaymentDate());

        return this;
    }
}

package uk.gov.hmcts.cmc.ccd.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Objects;

public class PaymentIntentionAssert extends AbstractAssert<PaymentIntentionAssert, PaymentIntention> {

    public PaymentIntentionAssert(PaymentIntention actual) {
        super(actual, PaymentIntentionAssert.class);
    }

    public PaymentIntentionAssert isEqualTo(CCDPaymentIntention paymentIntention) {
        isNotNull();

        if (!Objects.equals(actual.getPaymentOption().name(), paymentIntention.getPaymentOption().name())) {
            failWithMessage("Expected PaymentIntention.paymentOption to be <%s> but was <%s>",
                paymentIntention.getPaymentOption().name(), actual.getPaymentOption().name());
        }

        actual.getRepaymentPlan().ifPresent(repaymentPlan -> {
            if (!Objects.equals(repaymentPlan.getInstalmentAmount(), paymentIntention.getInstalmentAmount())) {
                failWithMessage("Expected PaymentIntention.instalmentAmount to be <%s> but was <%s>",
                    paymentIntention.getInstalmentAmount(), repaymentPlan.getInstalmentAmount());
            }
            if (!Objects.equals(repaymentPlan.getFirstPaymentDate(), paymentIntention.getFirstPaymentDate())) {
                failWithMessage("Expected PaymentIntention.firstPaymentDate to be <%s> but was <%s>",
                    paymentIntention.getFirstPaymentDate(), repaymentPlan.getFirstPaymentDate());
            }

            if (!Objects.equals(repaymentPlan.getPaymentSchedule().name(),
                paymentIntention.getPaymentSchedule().name())) {
                failWithMessage("Expected PaymentIntention.paymentSchedule to be <%s> but was <%s>",
                    paymentIntention.getPaymentSchedule().name(), repaymentPlan.getPaymentSchedule().name());
            }

            if (!Objects.equals(repaymentPlan.getPaymentLength(), paymentIntention.getPaymentLength())) {
                failWithMessage("Expected PaymentIntention.PaymentLength to be <%s> but was <%s>",
                    paymentIntention.getPaymentLength(), repaymentPlan.getPaymentLength());
            }

            if (!Objects.equals(repaymentPlan.getCompletionDate(), paymentIntention.getCompletionDate())) {
                failWithMessage("Expected PaymentIntention.paymentSchedule to be <%s> but was <%s>",
                    paymentIntention.getCompletionDate(), repaymentPlan.getCompletionDate());
            }
        }
        );

        actual.getPaymentDate().ifPresent(paymentDate -> {
            if (!Objects.equals(paymentDate, paymentIntention.getPaymentDate())) {
                failWithMessage("Expected PaymentIntention.paymentDate to be <%s> but was <%s>",
                    paymentIntention.getPaymentDate(), paymentDate);
            }
        });

        return this;
    }
}

package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import org.junit.Assert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Objects;

public class PaymentIntentionAssert extends AbstractAssert<uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.PaymentIntentionAssert, PaymentIntention> {

    public PaymentIntentionAssert(PaymentIntention actual) {
        super(actual, uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.PaymentIntentionAssert.class);
    }

    public PaymentIntentionAssert isEqualTo(CCDPaymentIntention paymentIntention) {
        isNotNull();

        if (!Objects.equals(actual.getPaymentOption().name(), paymentIntention.getPaymentOption().name())) {
            failWithMessage("Expected PaymentIntention.paymentOption to be <%s> but was <%s>",
                paymentIntention.getPaymentOption().name(), actual.getPaymentOption().name());
        }

        actual.getRepaymentPlan().ifPresent(
            repaymentPlan -> {
                Assert.assertEquals(repaymentPlan.getInstalmentAmount(), paymentIntention.getInstalmentAmount());
                Assert.assertEquals(repaymentPlan.getPaymentLength(), paymentIntention.getPaymentLength());
                Assert.assertEquals(repaymentPlan.getFirstPaymentDate(), paymentIntention.getFirstPaymentDate());
                Assert.assertEquals(repaymentPlan.getPaymentSchedule().getDescription(), paymentIntention.getPaymentSchedule().getDescription());
            });

        actual.getPaymentDate().ifPresent(paymentDate -> {
            if (!Objects.equals(paymentDate, paymentIntention.getPaymentDate())) {
                failWithMessage("Expected PaymentIntention.paymentDate to be <%s> but was <%s>",
                    paymentIntention.getPaymentDate(), paymentDate);
            }
        });

        return this;
    }
}

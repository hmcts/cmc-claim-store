package uk.gov.hmcts.cmc.ccd.deprecated.assertion.response;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class PaymentIntentionAssert extends AbstractAssert<PaymentIntentionAssert, PaymentIntention> {

    public PaymentIntentionAssert(PaymentIntention actual) {
        super(actual, PaymentIntentionAssert.class);
    }

    public PaymentIntentionAssert isEqualTo(CCDPaymentIntention paymentIntention) {
        /*isNotNull();

        if (!Objects.equals(actual.getPaymentOption().name(), paymentIntention.getPaymentOption().name())) {
            failWithMessage("Expected PaymentIntention.paymentOption to be <%s> but was <%s>",
                paymentIntention.getPaymentOption().name(), actual.getPaymentOption().name());
        }

        actual.getRepaymentPlan().ifPresent(
            repaymentPlan -> assertThat(repaymentPlan).isEqualTo(paymentIntention.getRepaymentPlan())
        );

        actual.getPaymentDate().ifPresent(paymentDate -> {
            if (!Objects.equals(paymentDate, paymentIntention.getPaymentDate())) {
                failWithMessage("Expected PaymentIntention.paymentDate to be <%s> but was <%s>",
                    paymentIntention.getPaymentDate(), paymentDate);
            }
        });*/

        return this;
    }
}

package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.domain.models.Payment;

import java.util.Objects;

public class PaymentAssert extends AbstractAssert<PaymentAssert, Payment> {

    public PaymentAssert(Payment actual) {
        super(actual, PaymentAssert.class);
    }

    public PaymentAssert isEqualTo(CCDPayment payment) {
        isNotNull();

        if (!Objects.equals(actual.getId(), payment.getId())) {
            failWithMessage("Expected Payment.id to be <%s> but was <%s>",
                payment.getId(), actual.getId());
        }

        if (!Objects.equals(actual.getStatus(), payment.getStatus())) {
            failWithMessage("Expected Payment.status to be <%s> but was <%s>",
                payment.getStatus(), actual.getStatus());
        }

        if (!Objects.equals(actual.getReference(), payment.getReference())) {
            failWithMessage("Expected Payment.reference to be <%s> but was <%s>",
                payment.getReference(), actual.getReference());
        }

        if (!Objects.equals(actual.getAmount(), payment.getAmount())) {
            failWithMessage("Expected Payment.amount to be <%s> but was <%s>",
                payment.getAmount(), actual.getAmount());
        }

        if (!Objects.equals(actual.getDateCreated(), payment.getDateCreated())) {
            failWithMessage("Expected Payment.dateCreated to be <%s> but was <%s>",
                payment.getDateCreated(), actual.getDateCreated());
        }

        return this;
    }

}

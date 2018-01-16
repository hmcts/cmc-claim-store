package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.domain.models.Payment;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class CCDPaymentAssert extends AbstractAssert<CCDPaymentAssert, CCDPayment> {

    public CCDPaymentAssert(CCDPayment actual) {
        super(actual, CCDPaymentAssert.class);
    }

    public CCDPaymentAssert isEqualTo(Payment payment) {
        isNotNull();

        if (!Objects.equals(actual.getId(), payment.getId())) {
            failWithMessage("Expected CCDPayment.id to be <%s> but was <%s>",
                payment.getId(), actual.getId());
        }

        if (!Objects.equals(actual.getDescription(), payment.getDescription())) {
            failWithMessage("Expected CCDPayment.description to be <%s> but was <%s>",
                payment.getDescription(), actual.getDescription());
        }

        if (!Objects.equals(actual.getReference(), payment.getReference())) {
            failWithMessage("Expected CCDPayment.reference to be <%s> but was <%s>",
                payment.getReference(), actual.getReference());
        }

        if (!Objects.equals(actual.getAmount(), payment.getAmount())) {
            failWithMessage("Expected CCDPayment.amount to be <%s> but was <%s>",
                payment.getAmount(), actual.getAmount());
        }

        if (!Objects.equals(actual.getDateCreated(), payment.getDateCreated())) {
            failWithMessage("Expected CCDPayment.dateCreated to be <%s> but was <%s>",
                payment.getDateCreated(), actual.getDateCreated());
        }

        assertThat(actual.getPaymentState()).isEqualTo(payment.getState());

        return this;
    }

}

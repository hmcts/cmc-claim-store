package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;

import java.util.Objects;

public class PaymentDeclarationAssert extends AbstractAssert<PaymentDeclarationAssert, PaymentDeclaration> {

    public PaymentDeclarationAssert(PaymentDeclaration actual) {
        super(actual, PaymentDeclarationAssert.class);
    }

    public PaymentDeclarationAssert isEqualTo(CCDPaymentDeclaration expected) {
        isNotNull();

        if (!Objects.equals(actual.getPaidDate(), expected.getPaidDate())) {
            failWithMessage("Expected CCDPaymentDeclaration.paidDate to be <%s> but was <%s>",
                expected.getPaidDate(), actual.getPaidDate());
        }

        if (!Objects.equals(actual.getExplanation(), expected.getExplanation())) {
            failWithMessage("Expected CCDPaymentDeclaration.explanation to be <%s> but was <%s>",
                expected.getExplanation(), actual.getExplanation());
        }

        return this;
    }

}

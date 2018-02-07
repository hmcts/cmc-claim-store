package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentState;
import uk.gov.hmcts.cmc.domain.models.PaymentState;

import java.util.Objects;

public class PaymentStateAssert extends AbstractAssert<PaymentStateAssert, PaymentState> {

    public PaymentStateAssert(PaymentState actual) {
        super(actual, PaymentStateAssert.class);
    }

    public PaymentStateAssert isEqualTo(CCDPaymentState ccdPaymentState) {
        isNotNull();

        if (!Objects.equals(actual.getStatus(), ccdPaymentState.getStatus())) {
            failWithMessage("Expected PaymentState.status to be <%s> but was <%s>",
                ccdPaymentState.getStatus(), actual.getStatus());
        }

        if (!Objects.equals(actual.isFinished(), ccdPaymentState.getFinished().equals("YES"))) {
            failWithMessage("Expected PaymentState.finished to be <%s> but was <%s>",
                ccdPaymentState.getFinished().equals("YES"), actual.isFinished());
        }
        return this;
    }


}

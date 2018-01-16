package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentState;
import uk.gov.hmcts.cmc.domain.models.PaymentState;

import java.util.Objects;

public class CCDPaymentStateAssert extends AbstractAssert<CCDPaymentStateAssert, CCDPaymentState> {

    public CCDPaymentStateAssert(CCDPaymentState actual) {
        super(actual, CCDPaymentStateAssert.class);
    }

    public CCDPaymentStateAssert isEqualTo(PaymentState paymentState) {
        isNotNull();

        if (!Objects.equals(actual.getStatus(), paymentState.getStatus())) {
            failWithMessage("Expected CCDPaymentState.status to be <%s> but was <%s>",
                paymentState.getStatus(), actual.getStatus());
        }

        if (!Objects.equals(actual.isFinished(), paymentState.isFinished())) {
            failWithMessage("Expected CCDPayment.finished to be <%s> but was <%s>",
                paymentState.isFinished(), actual.isFinished());
        }
        return this;
    }


}

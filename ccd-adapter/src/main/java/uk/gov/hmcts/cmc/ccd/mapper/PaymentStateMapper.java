package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentState;
import uk.gov.hmcts.cmc.domain.models.PaymentState;

@Component
public class PaymentStateMapper implements Mapper<CCDPaymentState, PaymentState> {

    @Override
    public CCDPaymentState to(PaymentState paymentState) {
        return CCDPaymentState
            .builder()
            .status(paymentState.getStatus())
            .finished(paymentState.isFinished() ? "YES" : "NO")
            .build();
    }

    @Override
    public PaymentState from(CCDPaymentState payment) {
        return new PaymentState(payment.getStatus(), "YES".equals(payment.getFinished()));
    }
}

package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.domain.models.Payment;

@Component
public class PaymentMapper implements Mapper<CCDPayment, Payment> {

    private final PaymentStateMapper paymentStateMapper;

    public PaymentMapper(PaymentStateMapper paymentStateMapper) {
        this.paymentStateMapper = paymentStateMapper;
    }

    @Override
    public CCDPayment to(Payment payment) {
        return CCDPayment
            .builder()
            .amount(payment.getAmount())
            .id(payment.getId())
            .reference(payment.getReference())
            .description(payment.getDescription())
            .dateCreated(payment.getDateCreated())
            .paymentState(paymentStateMapper.to(payment.getState()))
            .build();
    }

    @Override
    public Payment from(CCDPayment ccdPayment) {
        return new Payment(ccdPayment.getId(),
            ccdPayment.getAmount(),
            ccdPayment.getReference(),
            ccdPayment.getDescription(),
            ccdPayment.getDateCreated(),
            paymentStateMapper.from(ccdPayment.getPaymentState()));
    }
}

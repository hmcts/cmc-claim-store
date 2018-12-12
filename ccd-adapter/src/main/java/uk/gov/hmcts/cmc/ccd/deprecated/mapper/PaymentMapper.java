package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPayment;
import uk.gov.hmcts.cmc.domain.models.Payment;

//@Component
public class PaymentMapper implements Mapper<CCDPayment, Payment> {

    @Override
    public CCDPayment to(Payment payment) {
        if (payment == null) {
            return null;
        }

        return CCDPayment
            .builder()
            .amount(payment.getAmount())
            .id(payment.getId())
            .reference(payment.getReference())
            .dateCreated(payment.getDateCreated())
            .status(payment.getStatus())
            .build();
    }

    @Override
    public Payment from(CCDPayment ccdPayment) {
        if (ccdPayment == null) {
            return null;
        }

        return new Payment(
            ccdPayment.getId(),
            ccdPayment.getAmount(),
            ccdPayment.getReference(),
            ccdPayment.getDateCreated(),
            ccdPayment.getStatus()
        );
    }
}

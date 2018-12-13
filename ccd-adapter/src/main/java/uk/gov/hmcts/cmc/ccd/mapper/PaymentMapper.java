package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Payment;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class PaymentMapper implements BuilderMapper<CCDCase, Payment, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(Payment payment, CCDCase.CCDCaseBuilder builder) {
        if (payment == null) {
            return;
        }

        builder
            .paymentAmount(payment.getAmount())
            .paymentId(payment.getId())
            .paymentReference(payment.getReference())
            .paymentDateCreated(payment.getDateCreated())
            .paymentStatus(payment.getStatus());
    }

    @Override
    public Payment from(CCDCase ccdCase) {
        if (isBlank(ccdCase.getPaymentId())
            && ccdCase.getPaymentAmount() == null
            && isBlank(ccdCase.getPaymentReference())
            && isBlank(ccdCase.getPaymentDateCreated())
            && isBlank(ccdCase.getPaymentStatus())
        ) {
            return null;
        }

        return new Payment(
            ccdCase.getPaymentId(),
            ccdCase.getPaymentAmount(),
            ccdCase.getPaymentReference(),
            ccdCase.getPaymentDateCreated(),
            ccdCase.getPaymentStatus()
        );
    }
}

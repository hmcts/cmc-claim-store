package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;

@Component
public class PaymentDeclarationMapper implements Mapper<CCDPaymentDeclaration, PaymentDeclaration> {

    @Override
    public CCDPaymentDeclaration to(PaymentDeclaration paymentDeclaration) {
        return CCDPaymentDeclaration.builder()
            .paidDate(paymentDeclaration.getPaidDate())
            .explanation(paymentDeclaration.getExplanation())
            .build();
    }

    @Override
    public PaymentDeclaration from(CCDPaymentDeclaration paymentDeclaration) {
        if (paymentDeclaration == null) {
            return null;
        }

        return new PaymentDeclaration(paymentDeclaration.getPaidDate(), paymentDeclaration.getExplanation());
    }
}

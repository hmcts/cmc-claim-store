package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration.PaymentDeclarationBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SamplePaymentDeclaration {

    private SamplePaymentDeclaration() {
        super();
    }

    public static PaymentDeclarationBuilder builder() {
        return PaymentDeclaration.builder()
            .paidDate(LocalDate.of(2016, 1, 2))
            .paidAmount(new BigDecimal(100))
            .explanation("Paid cash");
    }
}


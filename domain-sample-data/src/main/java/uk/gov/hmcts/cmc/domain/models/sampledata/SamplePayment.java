package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Payment.PaymentBuilder;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

public class SamplePayment {

    public static final String PAYMENT_REFERENCE = "RC-1524-6488-1670-7520";

    private SamplePayment() {
        super();
    }

    public static PaymentBuilder builder() {
        return Payment.builder()
            .reference(PAYMENT_REFERENCE)
            .amount(new BigDecimal("40.99"))
            .dateCreated("2019-01-01")
            .id("PaymentId")
            .status(SUCCESS);
    }
}

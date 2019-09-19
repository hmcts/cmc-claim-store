package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Payment.PaymentBuilder;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

public class SamplePayment {

    private SamplePayment() {
        super();
    }

    public static PaymentBuilder builder() {
        return Payment.builder()
            .reference("RC-1524-6488-1670-7520")
            .amount(new BigDecimal("40.99"))
            .dateCreated("2019-01-01")
            .id("PaymentId")
            .nextUrl("http://nexturl.test")
            .status(SUCCESS);
    }
}

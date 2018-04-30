package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.Payment;

import java.math.BigDecimal;

public class SamplePayment {

    private BigDecimal amount = new BigDecimal("4000");
    private String reference = "RC-1524-6488-1670-7520";
    private String status = "success";

    public static SamplePayment builder() {
        return new SamplePayment();
    }

    public static Payment validDefaults() {
        return builder().build();
    }

    public Payment build() {
        return new Payment(null, amount, reference, null, status);
    }

}

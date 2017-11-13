package uk.gov.hmcts.cmc.claimstore.model.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.Payment;
import uk.gov.hmcts.cmc.claimstore.models.PaymentState;

import java.math.BigDecimal;

public class SamplePayment {

    private String id = "123";
    private BigDecimal amount = new BigDecimal("4000");
    private String reference = "reference";
    private String description = "description";
    private String dateCreated = "2010-10-12";
    private PaymentState paymentState = new PaymentState("status", true);

    public static SamplePayment builder() {
        return new SamplePayment();
    }

    public static Payment validDefaults() {
        return builder().build();
    }

    public Payment build() {
        return new Payment(id, amount, reference, description, dateCreated, paymentState);
    }

}

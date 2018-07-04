package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.response.HowMuchHaveYouPaid;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SamplePaymentDetails {
    private BigDecimal paidAmount = BigDecimal.valueOf(120);
    private LocalDate paymentDate = LocalDate.now();
    private String paymentMethod = "Cash Payment";

    public static SamplePaymentDetails builder() {
        return new SamplePaymentDetails();
    }

    public static HowMuchHaveYouPaid validDefaults() {
        return builder().build();
    }

    public SamplePaymentDetails withPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
        return this;
    }

    public SamplePaymentDetails withPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        return this;
    }

    public SamplePaymentDetails withPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public HowMuchHaveYouPaid build() {
        return new HowMuchHaveYouPaid(paidAmount, paymentDate, paymentMethod);
    }

}

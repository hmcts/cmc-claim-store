package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import java.time.LocalDate;

public class SamplePaymentDeclaration {

    private LocalDate paidDate = LocalDate.of(2016, 1, 2);
    private String explanation = "Paid cash";

    public static SamplePaymentDeclaration builder() {
        return new SamplePaymentDeclaration();
    }

    public static PaymentDeclaration validDefaults() {
        return builder().build();
    }

    public SamplePaymentDeclaration withPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
        return this;
    }

    public SamplePaymentDeclaration withExplanation(String explanation) {
        this.explanation = explanation;
        return this;
    }

    public PaymentDeclaration build() {
        return new PaymentDeclaration(paidDate, explanation);
    }

}


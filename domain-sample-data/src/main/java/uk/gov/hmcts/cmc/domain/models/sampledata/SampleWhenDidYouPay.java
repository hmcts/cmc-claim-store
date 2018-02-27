package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.WhenDidYouPay;
import java.time.LocalDate;

public class SampleWhenDidYouPay {

    private LocalDate paidDate = LocalDate.of(1968, 1, 2);
    private String explanation = "Paid cash";

    public static SampleWhenDidYouPay builder() {
        return new SampleWhenDidYouPay();
    }

    public static WhenDidYouPay validDefaults() {
        return builder().build();
    }

    public SampleWhenDidYouPay withPaidDate(final LocalDate whenDidYouPay) {
        this.paidDate = paidDate;
        return this;
    }

    public SampleWhenDidYouPay withExplanation(final String explanation) {
        this.explanation = explanation;
        return this;
    }

    public WhenDidYouPay build() {
        return new WhenDidYouPay(paidDate, explanation);
    }

}


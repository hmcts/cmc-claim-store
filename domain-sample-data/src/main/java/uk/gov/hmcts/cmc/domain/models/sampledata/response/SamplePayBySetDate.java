package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.response.PayBySetDate;
import java.time.LocalDate;

public class SamplePayBySetDate {
    private String explanation = "The amount to be paid";
    private LocalDate paymentDate = LocalDate.of(2019, 10, 10);

    public static SamplePayBySetDate builder() {
        return new SamplePayBySetDate();
    }

    public static PayBySetDate validDefaults() {
        return builder().build();
    }

    public SamplePayBySetDate withExplanation(String explanation) {
        this.explanation = explanation;
        return this;
    }

    public SamplePayBySetDate withPastDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        return this;
    }

    public PayBySetDate build() {
        return new PayBySetDate(paymentDate, explanation);
    }
}

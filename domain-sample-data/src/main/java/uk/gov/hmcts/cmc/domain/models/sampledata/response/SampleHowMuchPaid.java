package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.response.HowMuchPaid;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleHowMuchPaid {

    private String explanation = "The amount paid";
    private LocalDate pastDate = LocalDate.of(2016, 10, 10);
    private BigDecimal amount = new BigDecimal("100");


    public static SampleHowMuchPaid builder() {
        return new SampleHowMuchPaid();
    }

    public static HowMuchPaid validDefaults() {
        return builder().build();
    }

    public SampleHowMuchPaid withExplanation(String explanation) {
        this.explanation = explanation;
        return this;
    }

    public SampleHowMuchPaid withPastDate(LocalDate pastDate) {
        this.pastDate = pastDate;
        return this;
    }

    public SampleHowMuchPaid withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public HowMuchPaid build() {
        return new HowMuchPaid(amount, pastDate, explanation);
    }
}

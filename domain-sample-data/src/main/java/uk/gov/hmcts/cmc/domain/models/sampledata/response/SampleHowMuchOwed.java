package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import java.math.BigDecimal;

public class SampleHowMuchOwed {

    private String explanation = "I don't owe the amount";
    private BigDecimal amount = new BigDecimal("100");


    public static SampleHowMuchOwed builder() {
        return new SampleHowMuchOwed();
    }

    public static HowMuchOwed validDefaults() {
        return builder().build();
    }

    public SampleHowMuchOwed withExplanation(final String explanation) {
        this.explanation = explanation;
        return this;
    }

    public SampleHowMuchOwed withAmount(final BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public HowMuchOwed build() {
        return new HowMuchOwed(amount, explanation);
    }

}

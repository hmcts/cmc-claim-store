package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.response.HowMuchOwed;
import java.math.BigDecimal;

public class SampleHowMuchOwed {

    private String text = "I don't owe the amount";
    private BigDecimal amount = new BigDecimal("100");


    public static SampleHowMuchOwed builder() {
        return new SampleHowMuchOwed();
    }

    public static HowMuchOwed validDefaults() {
        return builder().build();
    }

    public HowMuchOwed build() {
        return new HowMuchOwed(amount, text);
    }

}

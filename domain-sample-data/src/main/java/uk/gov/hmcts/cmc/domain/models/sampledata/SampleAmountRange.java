package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange.AmountRangeBuilder;

import java.math.BigDecimal;

public class SampleAmountRange {

    private SampleAmountRange() {
        super();
    }

    public static AmountRangeBuilder builder() {
        return AmountRange.builder()
            .lowerValue(BigDecimal.valueOf(100.99))
            .higherValue(BigDecimal.valueOf(99000.99));
    }
}

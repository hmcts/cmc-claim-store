package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown.AmountBreakDownBuilder;

import java.math.BigDecimal;

import static java.util.Collections.singletonList;

public class SampleAmountBreakdown {

    private SampleAmountBreakdown() {
        super();
    }

    public static AmountBreakDownBuilder builder() {
        return AmountBreakDown.builder()
            .rows(singletonList(new AmountRow("reason", new BigDecimal("40"))));
    }
}

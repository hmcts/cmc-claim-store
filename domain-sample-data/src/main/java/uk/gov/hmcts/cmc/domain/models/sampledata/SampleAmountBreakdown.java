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
        AmountRow amountRow = AmountRow.builder()
            .id("359fda9d-e5fd-4d6e-9525-238642d0157d")
            .reason("reason")
            .amount(new BigDecimal("40.99"))
            .build();
        return AmountBreakDown.builder().rows(singletonList(amountRow));
    }

    public static AmountBreakDownBuilder withThousandAsAmount() {
        AmountRow amountRow = AmountRow.builder()
            .id("359fda9d-e5fd-4d6e-9525-238642d0157d")
            .reason("reason")
            .amount(new BigDecimal("1000.99"))
            .build();
        return AmountBreakDown.builder().rows(singletonList(amountRow));
    }
}

package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class SampleAmountBreakdown {

    private List<AmountRow> rows = asList(
        new AmountRow("reason", new BigDecimal("40")),
        new AmountRow(null, null),
        new AmountRow(null, null),
        new AmountRow(null, null)
    );

    public SampleAmountBreakdown clearRows() {
        this.rows = new ArrayList<>();
        return this;
    }

    public SampleAmountBreakdown withRows(List<AmountRow> rows) {
        this.rows = rows;
        return this;
    }

    public SampleAmountBreakdown addRow(AmountRow row) {
        rows.add(row);
        return this;
    }

    public static SampleAmountBreakdown builder() {
        return new SampleAmountBreakdown();
    }

    public static AmountBreakDown validDefaults() {
        return builder().build();
    }

    public AmountBreakDown build() {
        return new AmountBreakDown(rows);
    }

}

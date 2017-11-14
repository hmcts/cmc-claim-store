package uk.gov.hmcts.cmccase.models.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.hmcts.cmccase.models.constraints.MinTotalAmount;
import uk.gov.hmcts.cmccase.models.AmountRow;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AmountBreakDown implements Amount {

    @Valid
    @NotNull
    @MinTotalAmount("0.01")
    private final List<AmountRow> rows;

    public AmountBreakDown(final List<AmountRow> rows) {
        this.rows = rows;
    }

    @JsonIgnore
    public BigDecimal getTotalAmount() {
        return rows.stream()
            .map(AmountRow::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<AmountRow> getRows() {
        return rows;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AmountBreakDown that = (AmountBreakDown) other;
        return Objects.equals(rows, that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }

}

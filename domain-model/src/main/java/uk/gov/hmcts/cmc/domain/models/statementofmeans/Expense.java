package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Expense {

    private final String type;
    private final PaymentFrequency frequency;
    private final BigDecimal amountPaid;

    public Expense(String type, PaymentFrequency frequency, BigDecimal amountPaid) {
        this.type = type;
        this.frequency = frequency;
        this.amountPaid = amountPaid;
    }

    public String getType() {
        return type;
    }

    public PaymentFrequency getFrequency() {
        return frequency;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Expense expense = (Expense) other;
        return Objects.equals(type, expense.type)
            && frequency == expense.frequency
            && Objects.equals(amountPaid, expense.amountPaid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, frequency, amountPaid);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

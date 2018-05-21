package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Income {

    private final String type;
    private final PaymentFrequency frequency;
    private final BigDecimal amountReceived;

    public Income(final String incomeType, PaymentFrequency frequency, BigDecimal amountReceived) {
        this.type = incomeType;
        this.frequency = frequency;
        this.amountReceived = amountReceived;
    }

    public String getType() {
        return type;
    }

    public PaymentFrequency getFrequency() {
        return frequency;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Income income = (Income) other;
        return Objects.equals(type, income.type)
            && frequency == income.frequency
            && Objects.equals(amountReceived, income.amountReceived);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, frequency, amountReceived);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

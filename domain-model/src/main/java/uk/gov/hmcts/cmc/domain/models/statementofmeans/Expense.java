package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.constraints.ValidExpense;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@ValidExpense
public class Expense {

    public enum ExpenseType {
        MORTGAGE("Mortgage"),
        RENT("Rent"),
        COUNCIL_TAX("Council Tax"),
        GAS("Gas"),
        ELECTRICITY("Electricity"),
        WATER("Water"),
        TRAVEL("Travel (work or school)"),
        SCHOOL_COSTS("School costs (include clothing)"),
        FOOD_HOUSEKEEPING("Food and housekeeping"),
        TV_AND_BROADBAND("TV and broadband"),
        HIRE_PURCHASES("Hire purchase"),
        MOBILE_PHONE("Mobile phone"),
        MAINTENANCE_PAYMENTS("Maintenance payments"),
        OTHER("Other");

        String description;

        ExpenseType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    @NotNull
    private final ExpenseType type;

    private final String otherName;

    @NotNull
    private final PaymentFrequency frequency;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amountPaid;

    public Expense(
        ExpenseType type,
        String otherName,
        PaymentFrequency frequency,
        BigDecimal amountPaid
    ) {
        this.type = type;
        this.otherName = otherName;
        this.frequency = frequency;
        this.amountPaid = amountPaid;
    }

    public ExpenseType getType() {
        return type;
    }

    public Optional<String> getOtherName() {
        return Optional.ofNullable(otherName);
    }

    public PaymentFrequency getFrequency() {
        return frequency;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

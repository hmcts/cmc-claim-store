package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(value = "typeDescription", allowGetters = true)
@Builder
public class Income {

    public enum IncomeType {
        JOB("Income from your job"),
        UNIVERSAL_CREDIT("Universal Credit"),
        JOB_SEEKERS_ALLOWANCE_INCOME_BASES("Jobseeker's Allowance (income based)"),
        JOB_SEEKERS_ALLOWANCE_CONTRIBUTION_BASED("Jobseeker's Allowance (contribution based)"),
        INCOME_SUPPORT("Income Support"),
        WORKING_TAX_CREDIT("Working Tax Credit"),
        CHILD_TAX_CREDIT("Child Tax Credit"),
        CHILD_BENEFIT("Child Benefit"),
        COUNCIL_TAX_SUPPORT("Council Tax Support"),
        PENSION("Pension"),
        OTHER("Other");

        String description;

        IncomeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    @NotNull
    private final IncomeType type;

    private final String otherSource;

    @NotNull
    private final PaymentFrequency frequency;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
    private final BigDecimal amountReceived;

    public Income(IncomeType type,
                  String otherSource,
                  PaymentFrequency frequency,
                  BigDecimal amountReceived
    ) {
        this.type = type;
        this.otherSource = otherSource;
        this.frequency = frequency;
        this.amountReceived = amountReceived;
    }

    public IncomeType getType() {
        return type;
    }

    public Optional<String> getOtherSource() {
        return Optional.ofNullable(otherSource);
    }

    public PaymentFrequency getFrequency() {
        return frequency;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public String getTypeDescription() {
        return type.description;
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
        return type == income.type
            && Objects.equals(otherSource, income.otherSource)
            && frequency == income.frequency
            && Objects.equals(amountReceived, income.amountReceived);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, otherSource, frequency, amountReceived);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

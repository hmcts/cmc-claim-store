package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.constraints.ValidIncome;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
@ValidIncome
public class Income extends CollectionId {

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

        private final String description;

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
    @DecimalMin(value = "0.00")
    private final BigDecimal amount;

    @Builder
    public Income(String id,
                  IncomeType type,
                  String otherSource,
                  PaymentFrequency frequency,
                  BigDecimal amount
    ) {
        super(id);
        this.type = type;
        this.otherSource = otherSource;
        this.frequency = frequency;
        this.amount = amount;
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

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

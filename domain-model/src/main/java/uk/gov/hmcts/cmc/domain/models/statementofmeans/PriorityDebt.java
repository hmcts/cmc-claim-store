package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PriorityDebt extends CollectionId {

    public enum PriorityDebtType {
        MORTGAGE("Mortgage"),
        RENT("Rent"),
        COUNCIL_TAX_COMMUNITY_CHARGE("Council Tax or Community Charge"),
        GAS("Gas"),
        ELECTRICITY("Electricity"),
        WATER("Water"),
        MAINTENANCE_PAYMENTS("Maintenance Payments");

        private final String description;

        PriorityDebtType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    @NotNull
    private final PriorityDebtType type;

    @NotNull
    private final PaymentFrequency frequency;

    @NotNull
    @Money
    @DecimalMin(value = "0.00")
    private final BigDecimal amount;

    @Builder
    public PriorityDebt(
        String id,
        PriorityDebtType type,
        PaymentFrequency frequency,
        BigDecimal amount
    ) {
        super(id);
        this.type = type;
        this.frequency = frequency;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

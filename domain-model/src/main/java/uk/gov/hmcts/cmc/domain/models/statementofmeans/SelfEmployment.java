package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class SelfEmployment {

    @NotBlank
    private final String jobTitle;

    @NotNull
    @Money
    @DecimalMin(value = "0")
    private final BigDecimal annualTurnover;

    @Valid
    private final OnTaxPayments onTaxPayments;

    public SelfEmployment(
        String jobTitle,
        BigDecimal annualTurnover,
        OnTaxPayments onTaxPayments
    ) {
        this.jobTitle = jobTitle;
        this.annualTurnover = annualTurnover;
        this.onTaxPayments = onTaxPayments;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public BigDecimal getAnnualTurnover() {
        return annualTurnover;
    }

    public Optional<OnTaxPayments> getOnTaxPayments() {
        return Optional.ofNullable(onTaxPayments);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

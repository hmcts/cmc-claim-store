package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

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

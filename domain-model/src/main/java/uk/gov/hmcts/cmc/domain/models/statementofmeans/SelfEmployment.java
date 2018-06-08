package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class SelfEmployment {

    @NotBlank
    private final String jobTitle;

    @NotNull
    @Money
    @DecimalMin(value = "0.01")
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SelfEmployment that = (SelfEmployment) other;
        return Objects.equals(jobTitle, that.jobTitle)
            && Objects.equals(annualTurnover, that.annualTurnover)
            && Objects.equals(onTaxPayments, that.onTaxPayments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobTitle, annualTurnover, onTaxPayments);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

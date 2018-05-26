package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class SelfEmployed {

    private final String jobTitle;
    private final BigDecimal annualTurnover;
    private final YesNoOption behindOnTaxPayments;
    private final BigDecimal amountYouOwe;
    private final String reason;

    public SelfEmployed(
        String jobTitle,
        BigDecimal annualTurnover,
        YesNoOption behindOnTaxPayments,
        BigDecimal amountYouOwe,
        String reason
    ) {
        this.jobTitle = jobTitle;
        this.annualTurnover = annualTurnover;
        this.behindOnTaxPayments = behindOnTaxPayments;
        this.amountYouOwe = amountYouOwe;
        this.reason = reason;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public BigDecimal getAnnualTurnover() {
        return annualTurnover;
    }

    public YesNoOption isBehindOnTaxPayments() {
        return behindOnTaxPayments;
    }

    public BigDecimal getAmountYouOwe() {
        return amountYouOwe;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SelfEmployed that = (SelfEmployed) other;
        return behindOnTaxPayments == that.behindOnTaxPayments
            && Objects.equals(jobTitle, that.jobTitle)
            && Objects.equals(annualTurnover, that.annualTurnover)
            && Objects.equals(amountYouOwe, that.amountYouOwe)
            && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {

        return Objects.hash(jobTitle, annualTurnover, behindOnTaxPayments, amountYouOwe, reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

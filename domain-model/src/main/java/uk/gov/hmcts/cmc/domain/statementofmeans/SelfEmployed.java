package uk.gov.hmcts.cmc.domain.statementofmeans;

import java.math.BigDecimal;

public class SelfEmployed {

    private final String jobTitle;
    private final BigDecimal annualTurnover;
    private final boolean behindOnTaxPayments;
    private final BigDecimal amountYouOwe;
    private final String reason;

    public SelfEmployed(
        String jobTitle,
        BigDecimal annualTurnover,
        boolean behindOnTaxPayments,
        BigDecimal amountYouOwe,
        String reason
    ) {
        this.jobTitle = jobTitle;
        this.annualTurnover = annualTurnover;
        this.behindOnTaxPayments = behindOnTaxPayments;
        this.amountYouOwe = amountYouOwe;
        this.reason = reason;
    }
}

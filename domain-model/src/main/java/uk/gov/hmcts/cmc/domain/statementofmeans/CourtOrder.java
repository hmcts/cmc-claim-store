package uk.gov.hmcts.cmc.domain.statementofmeans;

import java.math.BigDecimal;

public class CourtOrder {

    private final String details;
    private final BigDecimal amountOwed;
    private final BigDecimal monthlyInstalmentAmount;

    public CourtOrder(String details, BigDecimal amountOwed, BigDecimal monthlyInstalmentAmount) {
        this.details = details;
        this.amountOwed = amountOwed;
        this.monthlyInstalmentAmount = monthlyInstalmentAmount;
    }
}

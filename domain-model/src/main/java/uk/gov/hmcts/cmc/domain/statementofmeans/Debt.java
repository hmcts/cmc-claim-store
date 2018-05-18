package uk.gov.hmcts.cmc.domain.statementofmeans;

import java.math.BigDecimal;

public class Debt {

    private final String description;
    private final BigDecimal totalOwed;
    private final BigDecimal monthlyPayments;

    public Debt(String description, BigDecimal totalOwed, BigDecimal monthlyPayments) {
        this.description = description;
        this.totalOwed = totalOwed;
        this.monthlyPayments = monthlyPayments;
    }
}

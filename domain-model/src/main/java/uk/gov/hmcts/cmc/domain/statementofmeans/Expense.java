package uk.gov.hmcts.cmc.domain.statementofmeans;

import java.math.BigDecimal;

public class Expense {

    private final String type;
    private final PaymentFrequency frequency;
    private final BigDecimal amountPaid;

    public Expense(String type, PaymentFrequency frequency, BigDecimal amountPaid) {
        this.type = type;
        this.frequency = frequency;
        this.amountPaid = amountPaid;
    }
}

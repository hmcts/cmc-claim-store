package uk.gov.hmcts.cmc.domain.statementofmeans;

import java.math.BigDecimal;

public class Income {

    private final String type;
    private final PaymentFrequency frequency;
    private final BigDecimal amountReceived;

    public Income(final String incomeType, PaymentFrequency frequency, BigDecimal amountReceived) {
        this.type = incomeType;
        this.frequency = frequency;
        this.amountReceived = amountReceived;
    }
}

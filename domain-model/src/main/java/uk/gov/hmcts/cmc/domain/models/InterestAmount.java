package uk.gov.hmcts.cmc.domain.models;

import java.math.BigDecimal;

public class InterestAmount {

    private BigDecimal amount;

    public InterestAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

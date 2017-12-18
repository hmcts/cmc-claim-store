package uk.gov.hmcts.cmc.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InterestAmount {

    private BigDecimal amount;

    public InterestAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

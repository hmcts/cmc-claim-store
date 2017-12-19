package uk.gov.hmcts.cmc.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InterestAmount {

    private BigDecimal amount;

    public InterestAmount(BigDecimal amount) {
        if (amount != null) {
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

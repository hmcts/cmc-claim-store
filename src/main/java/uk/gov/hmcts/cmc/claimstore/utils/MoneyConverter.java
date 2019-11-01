package uk.gov.hmcts.cmc.claimstore.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MoneyConverter {

    private MoneyConverter() {
        // Utility class
    }

    public static BigInteger convertPoundsToPennies(BigDecimal pounds) {
        if (pounds == null) {
            return null;
        }
        return BigInteger.valueOf(pounds.movePointRight(2).intValue());
    }
}

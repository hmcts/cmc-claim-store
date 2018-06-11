package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDExpense {

    private ExpenseType type;
    private String otherExpense;
    private CCDPaymentFrequency frequency;
    private BigDecimal amountPaid;

    public enum ExpenseType {
        MORTGAGE,
        RENT,
        COUNCIL_TAX,
        GAS,
        ELECTRICITY,
        WATER,
        TRAVEL,
        SCHOOL_COSTS,
        FOOD_HOUSEKEEPING,
        TV_AND_BROADBAND,
        HIRE_PURCHASES,
        MOBILE_PHONE,
        MAINTENANCE_PAYMENTS,
        OTHER
    }
}

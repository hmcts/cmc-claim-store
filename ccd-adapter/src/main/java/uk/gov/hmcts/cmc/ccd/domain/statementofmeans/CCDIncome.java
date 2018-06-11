package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDIncome {

    private IncomeType type;
    private String otherSource;
    private CCDPaymentFrequency frequency;
    private BigDecimal amountReceived;

    public enum IncomeType {
        JOB,
        UNIVERSAL_CREDIT,
        JOB_SEEKERS_ALLOWANCE_INCOME_BASES,
        JOB_SEEKERS_ALLOWANCE_CONTRIBUTION_BASED,
        INCOME_SUPPORT,
        WORKING_TAX_CREDIT,
        CHILD_TAX_CREDIT,
        CHILD_BENEFIT,
        COUNCIL_TAX_SUPPORT,
        PENSION,
        OTHER
    }
}

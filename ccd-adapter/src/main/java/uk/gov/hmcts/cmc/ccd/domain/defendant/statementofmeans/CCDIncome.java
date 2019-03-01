package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDIncome {
    private CCDIncomeType type;
    private String otherSource;
    private CCDPaymentFrequency frequency;
    private BigDecimal amountReceived;
}

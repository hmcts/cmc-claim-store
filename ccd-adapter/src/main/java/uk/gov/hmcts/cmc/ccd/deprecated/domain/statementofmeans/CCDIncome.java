package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import java.math.BigDecimal;

@Builder
@Value
public class CCDIncome {
    private Income.IncomeType type;
    private String otherSource;
    private PaymentFrequency frequency;
    private BigDecimal amountReceived;
}

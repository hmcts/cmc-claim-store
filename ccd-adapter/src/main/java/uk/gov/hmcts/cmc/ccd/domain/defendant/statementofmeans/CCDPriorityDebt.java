package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDPriorityDebt {
    private CCDPriorityDebtType type;

    private CCDPaymentFrequency frequency;

    private BigDecimal amount;
}

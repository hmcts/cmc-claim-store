package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.math.BigDecimal;

@Value
@Builder
public class CCDPriorityDebt {
    private PriorityDebt.PriorityDebtType type;

    private PaymentFrequency frequency;

    private BigDecimal amount;
}

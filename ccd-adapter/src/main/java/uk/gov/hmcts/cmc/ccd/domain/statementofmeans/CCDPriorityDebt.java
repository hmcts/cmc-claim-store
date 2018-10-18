package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

import java.math.BigDecimal;

public class CCDPriorityDebt {
    private CCDPriorityDebtType type;

    private PaymentFrequency frequency;

    private BigDecimal amount;
}

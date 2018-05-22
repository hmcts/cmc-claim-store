package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDIncome {
    private String type;
    private CCDPaymentFrequency frequency;
    private BigDecimal amountReceived;
}

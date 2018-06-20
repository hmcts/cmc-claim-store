package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDOnTaxPayments {
    private BigDecimal amountYouOwe;
    private String reason;
}

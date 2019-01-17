package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CCDDebt {
    private String description;
    private BigDecimal totalOwed;
    private BigDecimal monthlyPayments;
}

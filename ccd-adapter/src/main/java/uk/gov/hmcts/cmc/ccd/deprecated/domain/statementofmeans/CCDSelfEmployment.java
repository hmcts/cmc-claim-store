package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CCDSelfEmployment {
    private String jobTitle;
    private BigDecimal annualTurnover;
    private CCDOnTaxPayments onTaxPayments;
}

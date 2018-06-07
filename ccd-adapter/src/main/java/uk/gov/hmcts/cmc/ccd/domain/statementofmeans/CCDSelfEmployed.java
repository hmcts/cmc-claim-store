package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Value
@Builder
public class CCDSelfEmployed {
    private String jobTitle;
    private BigDecimal annualTurnover;
    private CCDYesNoOption behindOnTaxPayments;
    private BigDecimal amountYouOwe;
    private String reason;
}

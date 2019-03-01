package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Value
@Builder
public class CCDBankAccount {
    private CCDBankAccountType type;
    private CCDYesNoOption joint;
    private BigDecimal balance;
}

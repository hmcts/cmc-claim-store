package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;

import java.math.BigDecimal;

@Value
@Builder
public class CCDBankAccount {
    private BankAccount.BankAccountType type;
    private CCDYesNoOption joint;
    private BigDecimal balance;
}

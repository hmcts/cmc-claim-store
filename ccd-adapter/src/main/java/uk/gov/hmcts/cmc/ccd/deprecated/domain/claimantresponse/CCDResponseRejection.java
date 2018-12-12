package uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Builder
@Value
public class CCDResponseRejection {
    private BigDecimal amountPaid;

    private CCDYesNoOption freeMediationOption;

    private String reason;
}

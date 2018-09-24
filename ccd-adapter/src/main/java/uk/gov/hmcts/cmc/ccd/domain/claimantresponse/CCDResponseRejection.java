package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Builder
@Value
public class CCDResponseRejection {
    private BigDecimal amountPaid;

    private CCDYesNoOption freeMediationOption;

    private String reason;
}

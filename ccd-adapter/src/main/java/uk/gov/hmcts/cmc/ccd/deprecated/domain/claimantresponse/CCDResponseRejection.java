package uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;

@Builder
@Value
public class CCDResponseRejection {
    private BigDecimal amountPaid;

    private YesNoOption freeMediationOption;

    private String reason;
}

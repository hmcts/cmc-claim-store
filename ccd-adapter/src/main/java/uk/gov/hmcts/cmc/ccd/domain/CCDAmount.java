package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDAmount {
    private AmountType type;
    private CCDAmountRange amountRange;
}

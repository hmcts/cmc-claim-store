package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDAmount {
    private AmountType type;
    private CCDAmountRange amountRange;
    private CCDAmountBreakDown amountBreakDown;
    private CCDNotKnown notKnown;
}

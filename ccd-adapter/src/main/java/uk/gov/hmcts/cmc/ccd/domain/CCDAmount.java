package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CCDAmount {
    private AmountType type;
    private CCDAmountRange amountRange;
    private CCDAmountBreakDown amountBreakDown;
    private CCDNotKnown notKnown;
}

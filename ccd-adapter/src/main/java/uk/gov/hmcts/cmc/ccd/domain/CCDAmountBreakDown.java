package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CCDAmountBreakDown {
    private List<ListArrayElement<CCDAmountRow>> rows;
}

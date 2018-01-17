package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class CCDAmountBreakDown {
    private List<Map<String, CCDAmountRow>> rows;
}

package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CCDStatementOfMeans {
    private String reason;
}

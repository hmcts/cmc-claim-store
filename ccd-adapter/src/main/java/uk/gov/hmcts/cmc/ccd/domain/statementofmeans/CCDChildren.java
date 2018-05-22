package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDChildren {
    private Integer under11;
    private Integer between11and15;
    private Integer between16and19;
}

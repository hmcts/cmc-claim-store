package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDStatementOfTruth {
    private String signerName;
    private String signerRole;
}

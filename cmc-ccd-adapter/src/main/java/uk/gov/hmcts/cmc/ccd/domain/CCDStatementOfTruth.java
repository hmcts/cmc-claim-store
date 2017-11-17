package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDStatementOfTruth {
    private final String signerName;
    private final String signerRole;
}

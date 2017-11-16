package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatementOfTruth {
    private final String signerName;
    private final String signerRole;
}

package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class StatementOfTruth {
    private final String signerName;
    private final String signerRole;
}

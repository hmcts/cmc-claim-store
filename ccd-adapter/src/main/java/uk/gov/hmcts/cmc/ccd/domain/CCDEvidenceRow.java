package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDEvidenceRow {
    private CCDEvidenceType type;
    private String description;
}

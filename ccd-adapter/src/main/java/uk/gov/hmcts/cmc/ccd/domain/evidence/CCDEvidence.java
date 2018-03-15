package uk.gov.hmcts.cmc.ccd.domain.evidence;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.util.List;

@Value
@Builder
public class CCDEvidence {
    private List<CCDCollectionElement<CCDEvidenceRow>> rows;
}

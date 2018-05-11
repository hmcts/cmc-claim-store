package uk.gov.hmcts.cmc.ccd.domain.evidence;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

import java.util.List;

@Data
@Builder
public class CCDEvidence {
    private List<CCDCollectionElement<CCDEvidenceRow>> rows;

    @JsonCreator
    CCDEvidence(List<CCDCollectionElement<CCDEvidenceRow>> rows) {
        this.rows = rows;
    }
}

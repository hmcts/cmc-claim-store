package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType;

@Component
public class EvidenceRowMapper {

    public CCDCollectionElement<CCDEvidenceRow> to(EvidenceRow evidenceRow) {
        if (evidenceRow == null) {
            return null;
        }

        CCDEvidenceRow.CCDEvidenceRowBuilder builder = CCDEvidenceRow.builder();
        builder.type(CCDEvidenceType.valueOf(evidenceRow.getType().name()));
        evidenceRow.getDescription().ifPresent(builder::description);
        return CCDCollectionElement.<CCDEvidenceRow>builder().value(builder.build()).id(evidenceRow.getId()).build();
    }

    public EvidenceRow from(CCDCollectionElement<CCDEvidenceRow> row) {
        CCDEvidenceRow value = row.getValue();
        if (value == null || value.getType() == null) {
            return null;
        }
        return new EvidenceRow(row.getId(), EvidenceType.valueOf(value.getType().name()), value.getDescription());
    }
}

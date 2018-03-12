package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.EvidenceType;

public class EvidenceRowMapper implements Mapper<CCDEvidenceRow, EvidenceRow> {

    @Override
    public CCDEvidenceRow to(EvidenceRow evidenceRow) {
        CCDEvidenceRow.CCDEvidenceRowBuilder builder = CCDEvidenceRow.builder();
        builder.type(evidenceRow.getType().name());
        evidenceRow.getDescription().ifPresent(builder::description);
        return builder.build();
    }

    @Override
    public EvidenceRow from(CCDEvidenceRow ccdEvidenceRow) {
        if (ccdEvidenceRow == null) {
            return null;
        }
        EvidenceType type = ccdEvidenceRow.getType() != null ? EvidenceType.valueOf(ccdEvidenceRow.getType()) : null;
        return new EvidenceRow(type, ccdEvidenceRow.getDescription());
    }
}

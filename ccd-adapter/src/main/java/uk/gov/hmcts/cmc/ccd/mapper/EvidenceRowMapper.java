package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDEvidenceType;
import uk.gov.hmcts.cmc.domain.models.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.EvidenceType;

@Component
public class EvidenceRowMapper implements Mapper<CCDEvidenceRow, EvidenceRow> {

    @Override
    public CCDEvidenceRow to(EvidenceRow evidenceRow) {
        CCDEvidenceRow.CCDEvidenceRowBuilder builder = CCDEvidenceRow.builder();
        builder.type(CCDEvidenceType.valueOf(evidenceRow.getType().name()));
        evidenceRow.getDescription().ifPresent(builder::description);
        return builder.build();
    }

    @Override
    public EvidenceRow from(CCDEvidenceRow row) {
        if (row == null) {
            return null;
        }
        EvidenceType type = row.getType() != null ? EvidenceType.valueOf(row.getType().name()) : null;
        return new EvidenceRow(type, row.getDescription());
    }
}

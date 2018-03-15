package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EvidenceMapper implements Mapper<CCDEvidence, Evidence> {

    private final EvidenceRowMapper evidenceRowMapper;

    @Autowired
    public EvidenceMapper(EvidenceRowMapper evidenceRowMapper) {
        this.evidenceRowMapper = evidenceRowMapper;
    }

    @Override
    public CCDEvidence to(Evidence evidence) {
        if (evidence == null) {
            return null;
        }
        CCDEvidence.CCDEvidenceBuilder builder = CCDEvidence.builder();

        builder.rows(
            evidence.getRows()
                .stream()
                .map(evidenceRowMapper::to)
                .filter(Objects::nonNull)
                .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                .collect(Collectors.toList())
        );
        return builder.build();
    }

    @Override
    public Evidence from(CCDEvidence ccdEvidence) {
        if (ccdEvidence == null) {
            return null;
        }
        return new Evidence(
            ccdEvidence.getRows().stream()
                .map(CCDCollectionElement::getValue)
                .map(evidenceRowMapper::from)
                .collect(Collectors.toList())
        );
    }
}

package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EvidenceMapper implements BuilderMapper<CCDCase, Evidence, CCDCase.CCDCaseBuilder> {

    private final EvidenceRowMapper evidenceRowMapper;

    @Autowired
    public EvidenceMapper(EvidenceRowMapper evidenceRowMapper) {
        this.evidenceRowMapper = evidenceRowMapper;
    }

    @Override
    public void to(Evidence evidence, CCDCase.CCDCaseBuilder builder) {
        if (evidence == null || evidence.getRows() == null || evidence.getRows().isEmpty()) {
            return;
        }

        builder.evidence(
            evidence.getRows()
                .stream()
                .map(evidenceRowMapper::to)
                .filter(Objects::nonNull)
                .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                .collect(Collectors.toList())
        );
    }

    @Override
    public Evidence from(CCDCase ccdCase) {
        if (ccdCase.getEvidence() == null || ccdCase.getEvidence().isEmpty()) {
            return null;
        }
        return new Evidence(
            ccdCase.getEvidence().stream()
                .map(CCDCollectionElement::getValue)
                .map(evidenceRowMapper::from)
                .collect(Collectors.toList())
        );
    }
}

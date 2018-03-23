package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DefendantEvidenceMapper implements Mapper<CCDDefendantEvidence, DefendantEvidence> {

    private final EvidenceRowMapper evidenceRowMapper;

    @Autowired
    public DefendantEvidenceMapper(EvidenceRowMapper evidenceRowMapper) {
        this.evidenceRowMapper = evidenceRowMapper;
    }

    @Override
    public CCDDefendantEvidence to(DefendantEvidence evidence) {
        if (evidence == null) {
            return null;
        }
        CCDDefendantEvidence.CCDDefendantEvidenceBuilder builder = CCDDefendantEvidence.builder();

        builder.rows(
            evidence.getRows()
                .stream()
                .map(evidenceRowMapper::to)
                .filter(Objects::nonNull)
                .map(row -> CCDCollectionElement.<CCDEvidenceRow>builder().value(row).build())
                .collect(Collectors.toList())
        );

        evidence.getComment().ifPresent(builder::comment);

        return builder.build();
    }

    @Override
    public DefendantEvidence from(CCDDefendantEvidence ccdEvidence) {
        if (ccdEvidence == null) {
            return null;
        }
        return new DefendantEvidence(
            ccdEvidence.getRows().stream()
                .map(CCDCollectionElement::getValue)
                .map(evidenceRowMapper::from)
                .collect(Collectors.toList()),
            ccdEvidence.getComment()
        );
    }
}

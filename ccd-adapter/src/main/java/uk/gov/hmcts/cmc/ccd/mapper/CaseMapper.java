package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Arrays;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.toCaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Component
public class CaseMapper {

    private final ClaimMapper claimMapper;
    private final boolean isMigrated;

    public CaseMapper(ClaimMapper claimMapper, @Value("${migration.cases.flag:false}") boolean isMigrated) {
        this.claimMapper = claimMapper;
        this.isMigrated = isMigrated;
    }

    public CCDCase to(Claim claim) {
        final CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        claim.getClaimDocument(SEALED_CLAIM).ifPresent(document -> builder
            .sealedClaimDocument(CCDDocument.builder()
                .documentUrl(document.toString())
                .build())
        );

        claimMapper.to(claim, builder);

        return builder
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn())
            .submittedOn(claim.getCreatedAt())
            .features(claim.getFeatures() != null ? String.join(",", claim.getFeatures()) : null)
            .migratedFromClaimStore(isMigrated ? YES : NO)
            .caseName(toCaseName.apply(claim))
            .build();
    }

    public Claim from(CCDCase ccdCase) {
        Claim.ClaimBuilder builder = Claim.builder();
        claimMapper.from(ccdCase, builder);

        builder
            .id(ccdCase.getId())
            .submitterId(ccdCase.getSubmitterId())
            .externalId(ccdCase.getExternalId())
            .referenceNumber(ccdCase.getReferenceNumber())
            .createdAt(ccdCase.getSubmittedOn())
            .issuedOn(ccdCase.getIssuedOn())
            .submitterEmail(ccdCase.getSubmitterEmail());

        if (ccdCase.getFeatures() != null) {
            builder.features(Arrays.asList(ccdCase.getFeatures().split(",")));
        }

        return builder.build();
    }
}

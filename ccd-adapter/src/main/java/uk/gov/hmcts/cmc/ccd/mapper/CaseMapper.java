package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.net.URI;
import java.util.Arrays;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

@Component
public class CaseMapper {

    private final ClaimMapper claimMapper;

    public CaseMapper(ClaimMapper claimMapper) {
        this.claimMapper = claimMapper;
    }

    public CCDCase to(Claim claim) {
        final CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        if (claim.getLetterHolderId() != null) {
            builder.letterHolderId(claim.getLetterHolderId());
        }

        if (claim.getDefendantId() != null) {
            builder.defendantId(claim.getDefendantId());
        }

        claim.getSealedClaimDocument().ifPresent(document -> builder
            .sealedClaimDocument(CCDDocument.builder()
                .documentUrl(document.toString())
                .build())
        );

        claimMapper.to(claim.getClaimData(), builder);

        return builder
            .id(claim.getId())
            .externalId(claim.getExternalId())
            .referenceNumber(claim.getReferenceNumber())
            .submitterId(claim.getSubmitterId())
            .submitterEmail(claim.getSubmitterEmail())
            .issuedOn(claim.getIssuedOn())
            .submittedOn(claim.getCreatedAt())
            .responseDeadline(claim.getResponseDeadline())
            .moreTimeRequested(claim.isMoreTimeRequested() ? YES : NO)
            .features(claim.getFeatures() != null ? String.join(",", claim.getFeatures()) : null)
            .build();
    }

    public Claim from(CCDCase ccdCase) {

        Claim.ClaimBuilder builder = Claim.builder()
            .id(ccdCase.getId())
            .submitterId(ccdCase.getSubmitterId())
            .letterHolderId(ccdCase.getLetterHolderId())
            .defendantId(ccdCase.getDefendantId())
            .externalId(ccdCase.getExternalId())
            .referenceNumber(ccdCase.getReferenceNumber())
            .claimData(claimMapper.from(ccdCase))
            .createdAt(ccdCase.getSubmittedOn())
            .issuedOn(ccdCase.getIssuedOn())
            .responseDeadline(ccdCase.getResponseDeadline())
            .moreTimeRequested(ccdCase.getMoreTimeRequested() == YES)
            .submitterEmail(ccdCase.getSubmitterEmail());

        if (ccdCase.getFeatures() != null) {
            builder.features(Arrays.asList(ccdCase.getFeatures().split(",")));
        }

        if (ccdCase.getSealedClaimDocument() != null) {
            builder.sealedClaimDocument(URI.create(ccdCase.getSealedClaimDocument().getDocumentUrl()));
        }
        return builder.build();
    }
}

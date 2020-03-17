package uk.gov.hmcts.cmc.claimstore.filters;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DocumentsFilter {

    private static List<ClaimDocumentType> defendantViewableDocsType = Arrays.stream(ClaimDocumentType.values())
        .filter(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals)
        .collect(Collectors.toList());

    private static List<ClaimDocumentType> claimantViewableDocsType = Arrays.stream(ClaimDocumentType.values())
        .filter(ClaimDocumentType.SEALED_CLAIM::equals)
        .collect(Collectors.toList());

    private static Predicate<ClaimDocument> docsForDefendant = claimDocument -> defendantViewableDocsType.stream()
        .anyMatch(docType -> claimDocument.getDocumentType().equals(docType));

    private static Predicate<ClaimDocument> docsForClaimant = claimDocument -> claimantViewableDocsType.stream()
        .anyMatch(docType -> claimDocument.getDocumentType().equals(docType));

    public static Claim filterDocuments(Claim claim, UserDetails userDetails) {

        if (userDetails.isCaseworker()) {
            return claim; // No need to filter.
        }

        ClaimDocumentCollection claimDocs = claim.getClaimDocumentCollection().orElseThrow(IllegalStateException::new);

        ClaimDocumentCollection docsToReturn = new ClaimDocumentCollection();

        if (userDetails.getId().equals(claim.getDefendantId())) {
            claimDocs.getClaimDocuments().stream()
                .filter(docsForDefendant)
                .forEach(docsToReturn::addClaimDocument);
        } else if (userDetails.getId().equals(claim.getSubmitterId())) {
            claimDocs.getClaimDocuments().stream()
                .filter(docsForClaimant)
                .forEach(docsToReturn::addClaimDocument);
        }
        return claim.toBuilder()
            .claimDocumentCollection(docsToReturn)
            .build();
    }
}

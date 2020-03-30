package uk.gov.hmcts.cmc.claimstore.filters;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;

import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.rules.ClaimDocumentsAccessRule.claimantViewableDocsType;
import static uk.gov.hmcts.cmc.claimstore.rules.ClaimDocumentsAccessRule.defendantViewableDocsType;

public class DocumentsFilter {

    private static Predicate<ClaimDocument> docsForDefendant = claimDocument -> defendantViewableDocsType
        .contains(claimDocument.getDocumentType());

    private static Predicate<ClaimDocument> docsForClaimant = claimDocument -> claimantViewableDocsType
        .contains(claimDocument.getDocumentType());

    private DocumentsFilter() {
        // Do nothing constructor
    }

    public static Claim filterDocuments(Claim claim, UserDetails userDetails) {

        if (userDetails.isCaseworker() || claim.getClaimDocumentCollection().isEmpty()) {
            return claim; // No need to filter.
        }

        ClaimDocumentCollection claimDocs = claim.getClaimDocumentCollection().orElseThrow(IllegalStateException::new);

        ClaimDocumentCollection docsToReturn = new ClaimDocumentCollection();
        claimDocs.getClaimDocuments().stream()
            .filter(filterByRole(claim, userDetails))
            .forEach(docsToReturn::addClaimDocument);

        return claim.toBuilder()
            .claimDocumentCollection(docsToReturn)
            .build();
    }

    private static Predicate<ClaimDocument> filterByRole(Claim claim, UserDetails userLoggedIn) {
        if (userLoggedIn.getId().equals(claim.getDefendantId())) {
            return docsForDefendant;
        } else if (userLoggedIn.getId().equals(claim.getSubmitterId())) {
            return docsForClaimant;
        }
        return x -> false;
    }
}

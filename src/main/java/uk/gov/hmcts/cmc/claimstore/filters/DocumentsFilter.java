package uk.gov.hmcts.cmc.claimstore.filters;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;

import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.rules.ClaimDocumentsAccessRule.claimantViewableDocsType;
import static uk.gov.hmcts.cmc.claimstore.rules.ClaimDocumentsAccessRule.defendantViewableDocsType;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype.OCON9X;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.FORM;

public class DocumentsFilter {

    private static Predicate<ClaimDocument> docsForDefendant = claimDocument -> defendantViewableDocsType.get()
        .contains(claimDocument.getDocumentType());

    private static Predicate<ClaimDocument> docsForClaimant = claimDocument -> claimantViewableDocsType.get()
        .contains(claimDocument.getDocumentType());

    private DocumentsFilter() {
        // Do nothing constructor
    }

    public static Claim filterDocuments(Claim claim, UserDetails userDetails, boolean ctscEnabled) {

        if (userDetails.isCaseworker() || claim.getClaimDocumentCollection().isEmpty()) {
            return claim; // No need to filter.
        }

        ClaimDocumentCollection claimDocs = claim.getClaimDocumentCollection().orElseThrow(IllegalStateException::new);

        ClaimDocumentCollection docsToReturn = new ClaimDocumentCollection();
        claimDocs.getClaimDocuments().stream()
            .filter(filterByRole(claim, userDetails))
            .filter(document -> ctscEnabled || ! (document.getDocumentType() == GENERAL_LETTER))
            .forEach(docsToReturn::addClaimDocument);

        if (ctscEnabled) {
            claim.getScannedDocument(FORM, OCON9X).stream()
                .forEach(docsToReturn::addScannedDocument);
        }

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

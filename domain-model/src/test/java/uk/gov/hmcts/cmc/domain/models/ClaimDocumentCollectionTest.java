package uk.gov.hmcts.cmc.domain.models;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocument.builder;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.COVER_SHEET;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;

public class ClaimDocumentCollectionTest {

    @Test
    public void shouldGetDocumentForProvidedDocumentId() {
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection();

        Optional<ClaimDocument> claimDocument = claimDocumentCollection.getDocument("12345");
        assertTrue(claimDocument.isPresent());

        claimDocument = claimDocumentCollection.getDocument("456");
        assertTrue(claimDocument.isEmpty());
    }

    @Test
    public void shouldGetDocumentForProvidedDocumentType() {
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection();

        Optional<ClaimDocument> claimDocument = claimDocumentCollection.getDocument(COVER_SHEET);
        assertTrue(claimDocument.isPresent());

        claimDocument = claimDocumentCollection.getDocument(ClaimDocumentType.CLAIM_ISSUE_RECEIPT);
        assertTrue(claimDocument.isEmpty());
    }

    private ClaimDocumentCollection getClaimDocumentCollection() {
        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        claimDocumentCollection.addClaimDocument(builder().documentType(GENERAL_LETTER).id("123").build());
        claimDocumentCollection.addClaimDocument(builder().documentType(COVER_SHEET).id("12345").build());
        return claimDocumentCollection;
    }

}

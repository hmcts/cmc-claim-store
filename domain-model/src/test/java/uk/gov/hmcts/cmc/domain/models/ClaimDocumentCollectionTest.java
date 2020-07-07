package uk.gov.hmcts.cmc.domain.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocument.builder;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.COVER_SHEET;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;

public class ClaimDocumentCollectionTest {

    @Test
    public void shouldGetDocumentForProvidedDocumentId() {
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection();

        Optional<ClaimDocument> claimDocument = claimDocumentCollection.getDocument("12345");
        Assert.assertTrue(claimDocument.isPresent());

        claimDocument = claimDocumentCollection.getDocument("456");
        Assert.assertTrue(claimDocument.isEmpty());
    }

    @Test
    public void shouldGetDocumentForProvidedDocumentType() {
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection();

        Optional<ClaimDocument> claimDocument = claimDocumentCollection.getDocument(COVER_SHEET);
        Assert.assertTrue(claimDocument.isPresent());

        claimDocument = claimDocumentCollection.getDocument(ClaimDocumentType.CLAIM_ISSUE_RECEIPT);
        Assert.assertTrue(claimDocument.isEmpty());
    }

    private ClaimDocumentCollection getClaimDocumentCollection() {
        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
        claimDocumentCollection.addClaimDocument(builder().documentType(GENERAL_LETTER).id("123").build());
        claimDocumentCollection.addClaimDocument(builder().documentType(COVER_SHEET).id("12345").build());
        return claimDocumentCollection;
    }

}

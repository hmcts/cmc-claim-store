package uk.gov.hmcts.cmc.claimstore.filters;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentsFilterTest {

    private Claim claim;
    private static final UserDetails VALID_DEFENDANT
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail("kk@mm.com").build();

    @BeforeAll
    void setup() {
        claim = SampleClaim.builder()
            .withOrderDocument(URI.create("http://localhost/doc.pdf"))
            .withClaimIssueReceiptDocument(URI.create("http://localhost/doc.pdf"))
            .withSealedClaimDocument(URI.create("http://localhost/doc.pdf"))
            .withSettlementAgreementDocument(URI.create("http://localhost/doc.pdf"))
            .withOcon9xScannedDocument(URI.create("http://localhost/doc.pdf"))
            .withGeneralLetter(URI.create("http://localhost/doc.pdf"))
            .build();
    }

    @Test
    void filterDefendantViewableDocs() {

        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, VALID_DEFENDANT, true);

        List<ClaimDocument> claimDocsFromFilter = filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList());

        assertAll(
            () -> assertEquals(4, claimDocsFromFilter
                .size()),
            () -> assertTrue(claimDocsFromFilter.stream()
                .map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.SEALED_CLAIM::equals)),
            () -> assertFalse(claimDocsFromFilter.stream()
                .map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals)
            )
        );

    }

    @Test
    void filterClaimantViewableDocs() {
        UserDetails validClaimant
            = SampleUserDetails.builder().withUserId(USER_ID).withMail(SUBMITTER_EMAIL).build();

        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, validClaimant, true);

        List<ClaimDocument> claimDocsFromFilter = filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList());

        assertAll(
            () -> assertEquals(4, claimDocsFromFilter
                .size()),
            () -> assertTrue(claimDocsFromFilter.stream()
                .map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals)),
            () -> assertFalse(claimDocsFromFilter.stream()
                .map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.SEALED_CLAIM::equals)
            )
        );
    }

    @Test
    void filterCaseWorkerViewableDocs() {
        UserDetails validCaseworker
            = SampleUserDetails.builder().withUserId("5").withMail("cw@worker.com")
            .withRoles("caseworker-cmc").build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, validCaseworker, true);

        assertEquals(5, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .size());
    }

    @Test
    void filterUnAuthorisedViewableDocs() {
        UserDetails unrelatedUser
            = SampleUserDetails.builder().withUserId("18").withMail("unknown@worker.com")
            .build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, unrelatedUser, true);

        assertTrue(filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .isEmpty());
    }

    @Test
    void filterClaimThatHasNoDocuments() {
        Claim claimNoDoc = SampleClaim.builder().build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claimNoDoc, VALID_DEFENDANT, true);

        assertTrue(filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .isEmpty());
    }

    @Test
    void shouldIncludeGeneralLetterAndOcon9XScannedFormDocumentIfCtscEnabled() {
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, VALID_DEFENDANT, true);

        assertEquals(4, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .size());

        assertEquals(1, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getScannedDocuments)
            .orElse(Collections.emptyList())
            .size());
    }

    @Test
    void shouldNotIncludeGeneralLetterAndOcon9XScannedFormDocumentIfCtscNotEnabled() {
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, VALID_DEFENDANT, false);

        assertEquals(3, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .size());

        assertEquals(0, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getScannedDocuments)
            .orElse(Collections.emptyList())
            .size());
    }



}

package uk.gov.hmcts.cmc.claimstore.filters;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DocumentsFilterTest {

    private Claim claim;
    private static final UserDetails VALID_DEFENDANT
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail("kk@mm.com").build();

    @BeforeAll
    public void setup() {
        claim = SampleClaim.builder()
            .withOrderDocument(URI.create("http://localhost/doc.pdf"))
            .withClaimIssueReceiptDocument(URI.create("http://localhost/doc.pdf"))
            .withSealedClaimDocument(URI.create("http://localhost/doc.pdf"))
            .withSettlementAgreementDocument(URI.create("http://localhost/doc.pdf"))
            .build();
    }

    @Test
    public void filterDefendantViewableDocs() {

        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, VALID_DEFENDANT);

        List<ClaimDocument> claimDocsFromFilter = filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList());

        assertAll(
            () -> assertEquals(3, claimDocsFromFilter
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
    public void filterClaimantViewableDocs() {
        UserDetails validClaimant
            = SampleUserDetails.builder().withUserId(USER_ID).withMail(SUBMITTER_EMAIL).build();

        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, validClaimant);

        List<ClaimDocument> claimDocsFromFilter = filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList());

        assertAll(
            () -> assertEquals(3, claimDocsFromFilter
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
    public void filterCaseWorkerViewableDocs() {
        UserDetails validCaseworker
            = SampleUserDetails.builder().withUserId("5").withMail("cw@worker.com")
            .withRoles("caseworker-cmc").build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, validCaseworker);

        assertEquals(4, filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .size());
    }

    @Test
    public void filterUnAuthorisedViewableDocs() {
        UserDetails unrelatedUser
            = SampleUserDetails.builder().withUserId("18").withMail("unknown@worker.com")
            .build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, unrelatedUser);

        assertTrue(filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .isEmpty());
    }

    @Test
    public void filterClaimThatHasNoDocuments(){
        claim = SampleClaim.builder().build();
        Claim filteredClaimForDefendant = DocumentsFilter.filterDocuments(claim, VALID_DEFENDANT);

        assertTrue(filteredClaimForDefendant.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList())
            .isEmpty());
    }

}

package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.documents.CCJByAdmissionOrDeterminationPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantCCJRequestService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;

@ExtendWith(MockitoExtension.class)
class ClaimantCCJRequestServiceTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User USER = new User("authorisation", USER_DETAILS);
    @Mock
    private CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;
    @Mock
    private DocumentsService documentService;
    @Mock
    private ClaimService claimService;
    private ClaimantCCJRequestService service;
    private Claim claim;
    private PDF pdf;
    private CountyCourtJudgmentEvent event;

    @Nested
    @DisplayName("feature flag on tests")
    class FeatureFlagOnTests {

        @BeforeEach
        void setUp() {
            service = new ClaimantCCJRequestService(
                ccjByAdmissionOrDeterminationPdfService,
                documentService,
                true,
                claimService);
        }

        @Test
        void shouldCallDocumentServiceIfCCJByAdmission() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
            claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj)
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                    .builder().build())
                .build();
            claim.toBuilder().countyCourtJudgment(ccj);
            pdf = new PDF(
                buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
                PDF_CONTENT,
                CLAIM_ISSUE_RECEIPT
            );
            when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
            when(documentService.uploadToDocumentManagement(any(PDF.class),
                anyString(), any(Claim.class))).thenReturn(claim);
            event = new CountyCourtJudgmentEvent(claim, "authorisation");
            service.uploadDocumentToDocumentStore(event);
            verify(ccjByAdmissionOrDeterminationPdfService)
                .createPdf(claim);
            verify(documentService)
                .uploadToDocumentManagement(pdf, USER.getAuthorisation(), claim);
        }

        @Test
        void shouldCallDocumentServiceIfCCJByDetermination() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.DETERMINATION).build();
            claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj)
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                    .builder().build())
                .build();
            pdf = new PDF(
                buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
                PDF_CONTENT,
                CLAIM_ISSUE_RECEIPT
            );
            when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
            when(documentService.uploadToDocumentManagement(any(PDF.class),
                anyString(), any(Claim.class))).thenReturn(claim);
            event = new CountyCourtJudgmentEvent(claim, "authorisation");
            service.uploadDocumentToDocumentStore(event);
            verify(ccjByAdmissionOrDeterminationPdfService)
                .createPdf(claim);
            verify(documentService)
                .uploadToDocumentManagement(pdf, USER.getAuthorisation(), claim);
        }

        @Test
        void shouldNotCallDocumentServiceIfCCJByDefault() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.DEFAULT).build();
            claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj)
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                    .builder().build())
                .build();
            event = new CountyCourtJudgmentEvent(claim, "authorisation");
            service.uploadDocumentToDocumentStore(event);
            verify(ccjByAdmissionOrDeterminationPdfService, never())
                .createPdf(any());
            verify(documentService, never())
                .uploadToDocumentManagement(any(), any(), any());
            verify(claimService, never()).updateClaimState(any(), any(), any());
        }

        @Test
        void shouldUpdateCCDCaseStateToJudgmentRequestedIfCCJByAdmissionOrDetermination() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
            claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj)
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                    .builder().build())
                .build();
            pdf = new PDF(
                buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
                PDF_CONTENT,
                CLAIM_ISSUE_RECEIPT
            );
            event = new CountyCourtJudgmentEvent(claim, "authorisation");
            when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
            when(documentService.uploadToDocumentManagement(any(PDF.class),
                anyString(), any(Claim.class))).thenReturn(claim);
            service.uploadDocumentToDocumentStore(event);
            verify(claimService).updateClaimState("authorisation",
                claim, ClaimState.JUDGMENT_REQUESTED);
        }
    }

    @Nested
    @DisplayName("feature flag off tests")
    class FeatureFlagOffTests {

        @BeforeEach
        void setUp() {
            service = new ClaimantCCJRequestService(
                ccjByAdmissionOrDeterminationPdfService,
                documentService,
                false,
                claimService);
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
            claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj)
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                    .builder().build())
                .build();
            event = new CountyCourtJudgmentEvent(claim, "authorisation");
            service.uploadDocumentToDocumentStore(event);
        }

        @Test
        void shouldNotCallDocumentServiceAndUpdateCCDCaseStateToOpenIfCCJByAdmissionOrDetermination() {
            verify(ccjByAdmissionOrDeterminationPdfService, never())
                .createPdf(any());
            verify(documentService, never())
                .uploadToDocumentManagement(any(), any(), any());
            verify(claimService).updateClaimState("authorisation",
                claim, ClaimState.OPEN);
        }
    }
}

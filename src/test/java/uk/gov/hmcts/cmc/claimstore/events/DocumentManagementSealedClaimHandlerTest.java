package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentManagementClaimIssuedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentManagementSealedClaimHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.SealedClaimToDocumentStoreUploader;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.services.SealedClaimToDocumentStoreUploader.APPLICATION_PDF;
import static uk.gov.hmcts.cmc.claimstore.services.SealedClaimToDocumentStoreUploader.PDF_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementSealedClaimHandlerTest {
    private static final byte[] N1_FORM_PDF = {65, 66, 67, 68};
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String DOCUMENT_MANAGEMENT_SELF_PATH = "/self/uri/path";

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ClaimService claimService;
    @Mock
    private CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    @Mock
    private LegalSealedClaimPdfService legalSealedClaimPdfService;
    @Mock
    private DocumentManagementClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler;

    private DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler;

    @Before
    public void setup() {
        documentManagementSealedClaimHandler = new DocumentManagementSealedClaimHandler(
            new SealedClaimToDocumentStoreUploader(
                documentManagementService,
                claimService,
                legalSealedClaimPdfService,
                citizenSealedClaimPdfService
            ),
            claimIssuedStaffNotificationHandler
        );

        when(citizenSealedClaimPdfService.createPdf(CLAIM, CLAIMANT_EMAIL)).thenReturn(N1_FORM_PDF);
        when(legalSealedClaimPdfService.createPdf(CLAIM)).thenReturn(N1_FORM_PDF);
    }

    @Test
    public void shouldUploadCitizenSealedClaimForm() {
        final ClaimIssuedEvent claimIssuedEvent = new ClaimIssuedEvent(CLAIM, PIN, SUBMITTER_NAME, AUTHORISATION);

        final String originalFileName = CLAIM.getReferenceNumber() + PDF_EXTENSION;

        when(documentManagementService.uploadDocument(AUTHORISATION, originalFileName,
            N1_FORM_PDF, APPLICATION_PDF)).thenReturn(DOCUMENT_MANAGEMENT_SELF_PATH);

        documentManagementSealedClaimHandler.uploadCitizenSealedClaimToDocumentStore(claimIssuedEvent);

        verify(citizenSealedClaimPdfService, once()).createPdf(CLAIM, CLAIMANT_EMAIL);

        verify(documentManagementService, once()).uploadDocument(AUTHORISATION,
            originalFileName, N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, once())
            .linkSealedClaimDocument(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);

        verify(claimIssuedStaffNotificationHandler).onClaimIssued(eq(claimIssuedEvent));
    }

    @Test
    public void shouldUploadLegalSealedClaimForm() throws NotificationClientException {
        final String originalFileName = CLAIM.getReferenceNumber() + PDF_EXTENSION;

        when(documentManagementService.uploadDocument(AUTHORISATION, originalFileName,
            N1_FORM_PDF, APPLICATION_PDF)).thenReturn(DOCUMENT_MANAGEMENT_SELF_PATH);

        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        documentManagementSealedClaimHandler
            .uploadRepresentativeSealedClaimToDocumentStore(representedClaimIssuedEvent);

        verify(legalSealedClaimPdfService, once()).createPdf(CLAIM);

        verify(documentManagementService, once()).uploadDocument(AUTHORISATION,
            originalFileName, N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, once())
            .linkSealedClaimDocument(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);

        verify(claimIssuedStaffNotificationHandler).onRepresentedClaimIssued(eq(representedClaimIssuedEvent));
    }

}

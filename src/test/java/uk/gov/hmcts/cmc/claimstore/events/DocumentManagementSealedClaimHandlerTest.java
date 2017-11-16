package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.DocumentManagementSealedClaimHandler.APPLICATION_PDF;
import static uk.gov.hmcts.cmc.claimstore.events.DocumentManagementSealedClaimHandler.PDF_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
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

    private DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler;

    @Before
    public void setup() {
        documentManagementSealedClaimHandler = new DocumentManagementSealedClaimHandler(
            documentManagementService,
            citizenSealedClaimPdfService,
            legalSealedClaimPdfService,
            claimService,
            true
        );

        when(citizenSealedClaimPdfService.createPdf(CLAIM, CLAIMANT_EMAIL)).thenReturn(N1_FORM_PDF);
        when(legalSealedClaimPdfService.createPdf(CLAIM)).thenReturn(N1_FORM_PDF);
    }

    @Test
    public void shouldUploadCitizenSealedClaimForm() {
        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(CLAIM, Optional.of(PIN), Optional.of(SUBMITTER_NAME), AUTHORISATION);

        when(documentManagementService.uploadDocument(AUTHORISATION, CLAIM.getReferenceNumber() + PDF_EXTENSION,
            N1_FORM_PDF, APPLICATION_PDF)).thenReturn(DOCUMENT_MANAGEMENT_SELF_PATH);

        documentManagementSealedClaimHandler.uploadCitizenSealedClaimToEvidenceStore(claimIssuedEvent);

        verify(citizenSealedClaimPdfService, once()).createPdf(CLAIM, CLAIMANT_EMAIL);

        verify(documentManagementService, once()).uploadDocument(AUTHORISATION,
            CLAIM.getReferenceNumber() + PDF_EXTENSION, N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, once())
            .linkSealedClaimDocumentManagementPath(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);

    }

    @Test
    public void shouldNotUploadCitizenSealedClaimFormWhenFeatureToggleIsOff() throws NotificationClientException {
        documentManagementSealedClaimHandler = new DocumentManagementSealedClaimHandler(
            documentManagementService,
            citizenSealedClaimPdfService,
            legalSealedClaimPdfService,
            claimService,
            false
        );

        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(CLAIM, Optional.of(PIN), Optional.of(SUBMITTER_NAME), AUTHORISATION);

        documentManagementSealedClaimHandler.uploadCitizenSealedClaimToEvidenceStore(claimIssuedEvent);

        verify(citizenSealedClaimPdfService, never()).createPdf(CLAIM, CLAIMANT_EMAIL);

        verify(documentManagementService, never()).uploadDocument(AUTHORISATION,
            CLAIM.getReferenceNumber() + PDF_EXTENSION, N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, never())
            .linkSealedClaimDocumentManagementPath(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);
    }


    @Test
    public void shouldUploadLegalSealedClaimForm() throws NotificationClientException {
        when(documentManagementService.uploadDocument(AUTHORISATION, CLAIM.getReferenceNumber() + PDF_EXTENSION,
            N1_FORM_PDF, "application/pdf")).thenReturn(DOCUMENT_MANAGEMENT_SELF_PATH);

        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, Optional.of(SUBMITTER_NAME), AUTHORISATION);

        documentManagementSealedClaimHandler
            .uploadRepresentativeSealedClaimToEvidenceStore(representedClaimIssuedEvent);

        verify(legalSealedClaimPdfService, once()).createPdf(CLAIM);

        verify(documentManagementService, once()).uploadDocument(AUTHORISATION,
            CLAIM.getReferenceNumber() + PDF_EXTENSION, N1_FORM_PDF, "application/pdf");

        verify(claimService, once())
            .linkSealedClaimDocumentManagementPath(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);
    }

    @Test
    public void shouldNotUploadLegalSealedClaimFormWhenFeatureToggleIsOff() throws NotificationClientException {
        documentManagementSealedClaimHandler = new DocumentManagementSealedClaimHandler(
            documentManagementService,
            citizenSealedClaimPdfService,
            legalSealedClaimPdfService,
            claimService,
            false
        );

        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, Optional.of(SUBMITTER_NAME), AUTHORISATION);

        documentManagementSealedClaimHandler
            .uploadRepresentativeSealedClaimToEvidenceStore(representedClaimIssuedEvent);

        verify(legalSealedClaimPdfService, never()).createPdf(CLAIM);

        verify(documentManagementService, never()).uploadDocument(AUTHORISATION,
            CLAIM.getReferenceNumber(), N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, never())
            .linkSealedClaimDocumentManagementPath(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);
    }

}

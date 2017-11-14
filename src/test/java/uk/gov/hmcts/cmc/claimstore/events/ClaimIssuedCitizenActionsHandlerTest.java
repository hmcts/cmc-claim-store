package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleClaim.getClaimWithNoDefendantEmail;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedCitizenActionsHandlerTest {
    private static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";
    private static final String DEFENDANT_CLAIM_ISSUED_TEMPLATE = "defendantClaimIssued";
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final byte[] N1_FORM_PDF = {65, 66, 67, 68};
    private static final String DOCUMENT_MANAGEMENT_SELF_PATH = "/self/uri/path";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String PDF_EXTENSION = ".pdf";

    private ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ClaimService claimService;
    @Mock
    private CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    @Mock
    private NotificationsProperties properties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        claimIssuedCitizenActionsHandler = new ClaimIssuedCitizenActionsHandler(
            claimIssuedNotificationService,
            properties,
            documentManagementService,
            citizenSealedClaimPdfService,
            claimService,
            true);

        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getClaimantClaimIssued()).thenReturn(CLAIMANT_CLAIM_ISSUED_TEMPLATE);
        when(emailTemplates.getDefendantClaimIssued()).thenReturn(DEFENDANT_CLAIM_ISSUED_TEMPLATE);
        when(citizenSealedClaimPdfService.createPdf(CLAIM, CLAIMANT_EMAIL)).thenReturn(N1_FORM_PDF);
    }

    @Test
    public void sendNotificationsSendsNotificationsToClaimantAndDefendant() throws NotificationClientException {

        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(CLAIM, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME, AUTHORISATION);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(CLAIM,
            CLAIMANT_EMAIL, Optional.empty(), CLAIMANT_CLAIM_ISSUED_TEMPLATE,
            "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);

        verify(claimIssuedNotificationService, once()).sendMail(CLAIM,
            SampleClaimIssuedEvent.DEFENDANT_EMAIL, Optional.of(SampleClaimIssuedEvent.PIN),
            DEFENDANT_CLAIM_ISSUED_TEMPLATE,
            "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }

    @Test
    public void sendNotificationsSendsNotificationToClaimantOnly() throws NotificationClientException {

        Claim claimNoDefendantEmail = getClaimWithNoDefendantEmail();

        ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(claimNoDefendantEmail, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME, AUTHORISATION);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(claimNoDefendantEmail,
            CLAIMANT_EMAIL, Optional.empty(), CLAIMANT_CLAIM_ISSUED_TEMPLATE,
            "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);

        verify(claimIssuedNotificationService, never()).sendMail(claimNoDefendantEmail,
            SampleClaimIssuedEvent.DEFENDANT_EMAIL, Optional.of(SampleClaimIssuedEvent.PIN),
            DEFENDANT_CLAIM_ISSUED_TEMPLATE,
            "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }

    @Test
    public void shouldUploadSealedClaimForm() throws NotificationClientException {
        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(CLAIM, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME, AUTHORISATION);

        when(documentManagementService.uploadSingleDocument(AUTHORISATION, CLAIM.getReferenceNumber() + PDF_EXTENSION,
            N1_FORM_PDF, APPLICATION_PDF)).thenReturn(DOCUMENT_MANAGEMENT_SELF_PATH);

        claimIssuedCitizenActionsHandler.uploadDocumentToEvidenceStore(claimIssuedEvent);

        verify(citizenSealedClaimPdfService, once()).createPdf(CLAIM, CLAIMANT_EMAIL);

        verify(documentManagementService, once()).uploadSingleDocument(AUTHORISATION,
            CLAIM.getReferenceNumber() + PDF_EXTENSION, N1_FORM_PDF, APPLICATION_PDF);

        verify(claimService, once()).linkDocumentManagement(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);

    }

    @Test
    public void shouldNotUploadSealedClaimFormWhenFeatureToggleIsOff() throws NotificationClientException {
        claimIssuedCitizenActionsHandler = new ClaimIssuedCitizenActionsHandler(
            claimIssuedNotificationService,
            properties,
            documentManagementService,
            citizenSealedClaimPdfService,
            claimService,
            false);

        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(CLAIM, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME, AUTHORISATION);

        claimIssuedCitizenActionsHandler.uploadDocumentToEvidenceStore(claimIssuedEvent);

        verify(citizenSealedClaimPdfService, never()).createPdf(CLAIM, CLAIMANT_EMAIL);

        verify(documentManagementService, never()).uploadSingleDocument(AUTHORISATION,
            CLAIM.getReferenceNumber() + PDF_EXTENSION, N1_FORM_PDF, APPLICATION_PDF);
        verify(claimService, never()).linkDocumentManagement(CLAIM.getId(), DOCUMENT_MANAGEMENT_SELF_PATH);
    }
}

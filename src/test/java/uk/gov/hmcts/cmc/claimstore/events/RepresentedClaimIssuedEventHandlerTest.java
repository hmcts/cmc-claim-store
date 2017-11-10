package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class RepresentedClaimIssuedEventHandlerTest {
    private static final String REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE = "representativeClaimIssued";
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final byte[] N1_FORM_PDF = {65, 66, 67, 68};

    private RepresentativeConfirmationHandler representativeConfirmationHandler;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private LegalSealedClaimPdfService legalSealedClaimPdfService;
    @Mock
    private NotificationsProperties properties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        representativeConfirmationHandler = new RepresentativeConfirmationHandler(
            claimIssuedNotificationService,
            properties,
            documentManagementService,
            legalSealedClaimPdfService,
            true);
        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getRepresentativeClaimIssued()).thenReturn(REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE);
        when(legalSealedClaimPdfService.createPdf(CLAIM)).thenReturn(N1_FORM_PDF);
    }

    @Test
    public void sendNotificationsSendsNotificationsToRepresentative() throws NotificationClientException {

        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        representativeConfirmationHandler.sendConfirmation(representedClaimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(CLAIM,
            CLAIMANT_EMAIL, Optional.empty(), REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE,
            "representative-issue-notification-" + representedClaimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }

    @Test
    public void shouldUploadSealedClaimForm() throws NotificationClientException {
        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        representativeConfirmationHandler.handleDocumentUpload(representedClaimIssuedEvent);

        verify(legalSealedClaimPdfService, once()).createPdf(CLAIM);
        verify(documentManagementService, once()).storeClaimN1Form(AUTHORISATION, CLAIM, N1_FORM_PDF);
    }

    @Test
    public void shouldNotUploadSealedClaimFormWhenFeatureToggleIsOff() throws NotificationClientException {
        representativeConfirmationHandler = new RepresentativeConfirmationHandler(
            claimIssuedNotificationService,
            properties,
            documentManagementService,
            legalSealedClaimPdfService,
            false);

        final RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        representativeConfirmationHandler.handleDocumentUpload(representedClaimIssuedEvent);

        verify(legalSealedClaimPdfService, never()).createPdf(CLAIM);
        verify(documentManagementService, never()).storeClaimN1Form(AUTHORISATION, CLAIM, N1_FORM_PDF);
    }
}

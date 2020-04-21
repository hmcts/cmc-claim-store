package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestedCitizenNotificationHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";
    private static final String CLAIMANT_TEMPLATE_ID = "claimant template id";

    private String generalLetterTemplateId;

    private MoreTimeRequestedCitizenNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Mock
    private GeneralLetterService generalLetterService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private UserService userService;

    @Before
    public void setup() {
        Claim claim = SampleClaim.builder().build();
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantMoreTimeRequested()).thenReturn(DEFENDANT_TEMPLATE_ID);
        when(emailTemplates.getClaimantMoreTimeRequested()).thenReturn(CLAIMANT_TEMPLATE_ID);

        handler = new MoreTimeRequestedCitizenNotificationHandler(
                notificationService,
                notificationsProperties,
                generalLetterService,
                caseDetailsConverter,
                docAssemblyService,
                userService,
                generalLetterTemplateId
        );
    }

    @Test
    public void sendEmailToLinkedDefendant() {
        verify(notificationService, once()).sendMail(
            eq(claim.getDefendantEmail()),
            eq(DEFENDANT_TEMPLATE_ID),
            anyMap(),
            eq(SampleMoreTimeRequestedEvent.getReference("defendant", claim.getReferenceNumber()))
        );
    }

    @Test
    public void sendEmailToClaimant() {
        verify(notificationService, once()).sendMail(
                eq(claim.getClaimant()),
                eq(CLAIMANT_TEMPLATE_ID),
                anyMap(),
                eq(SampleMoreTimeRequestedEvent.getReference("claimant", claim.getReferenceNumber()))
        );
    }

    @Test
    public void sendLetterToNotLinkedDefendant() {
    }

}

package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClient;

import java.util.Map;

public abstract class BaseNotificationServiceTest {
    protected static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";
    protected static final String DEFENDANT_RESPONSE_TEMPLATE = "defendantResponse";
    protected static final String CLAIMANT_CCJ_REQUESTED_TEMPLATE = "claimantCcjRequested";
    protected static final String FRONTEND_BASE_URL = "http://some.host.dot.com";
    protected static final String RESPOND_TO_CLAIM_URL = "http://some.host.dot.com/first-contact/start";
    protected static final String USER_EMAIL = "user@example.com";
    protected static final String USER_FULLNAME = "Steven Patrick";
    protected final Claim claim = SampleClaim.getDefault();

    @Mock
    protected NotificationClient notificationClient;
    @Mock
    protected NotificationsProperties properties;
    @Mock
    protected NotificationTemplates templates;
    @Mock
    protected EmailTemplates emailTemplates;

    @Captor
    protected ArgumentCaptor<Map<String, String>> templateParameters;
}

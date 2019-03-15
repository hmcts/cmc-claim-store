package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseNotificationServiceTest {
    protected static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";

    protected static final String CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT_TO_CLAIMANT_TEMPLATE
        = "claimantSignedSettlementAgreementToClaimant";

    protected static final String RESPONSE_BY_CLAIMANT_EMAIL_TO_DEFENDANT = "responseByClaimantEmailToDefendant";
    protected static final String DEFENDANT_RESPONSE_TEMPLATE = "fullDefence";
    protected static final String DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE = "fullDefence-noMediation";
    protected static final String CLAIMANT_CCJ_REQUESTED_TEMPLATE = "claimantCcjRequested";
    protected static final String CLAIMANT_SAYS_DEFENDANT_PAID_IN_FULL_TEMPLATE = "claimant-says-paid-in-full";
    protected static final String DEFENDANT_RESPOND_BY_ADMISSION = "defenceResponseByAdmissions";

    protected static final String FRONTEND_BASE_URL = "http://some.host.dot.com";
    protected static final String RESPOND_TO_CLAIM_URL = "http://some.host.dot.com/first-contact/start";
    protected static final String USER_EMAIL = "user@example.com";
    protected static final String USER_FULLNAME = "Steven Patrick";

    private PrintStream systemOut;
    private ByteArrayOutputStream outContent;

    protected final Claim claim = SampleClaim.getDefault();

    @Mock
    protected NotificationClient notificationClient;
    @Mock
    protected NotificationsProperties properties;
    @Mock
    protected NotificationTemplates templates;
    @Mock
    protected EmailTemplates emailTemplates;

    @Mock
    protected AppInsights appInsights;

    @Captor
    protected ArgumentCaptor<Map<String, String>> templateParameters;

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        systemOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        System.setOut(systemOut);
    }

    protected void assertWasLogged(CharSequence text) {
        String logContent = outContent.toString();
        assertThat(logContent).contains(text);
    }

    protected void assertWasNotLogged(CharSequence text) {
        String logContent = outContent.toString();
        assertThat(logContent).doesNotContain(text);
    }
}

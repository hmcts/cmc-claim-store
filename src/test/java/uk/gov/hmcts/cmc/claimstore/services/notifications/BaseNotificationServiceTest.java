package uk.gov.hmcts.cmc.claimstore.services.notifications;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public abstract class BaseNotificationServiceTest {

    protected static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";

    protected static final String CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT_TO_CLAIMANT_TEMPLATE
        = "claimantSignedSettlementAgreementToClaimant";

    protected static final String RESPONSE_BY_CLAIMANT_EMAIL_TO_DEFENDANT = "responseByClaimantEmailToDefendant";
    protected static final String DEFENDANT_RESPONSE_TEMPLATE = "fullDefence";
    protected static final String DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE = "fullDefence-noMediation";
    protected static final String ONLINE_DQ_WITH_NO_MEDIATION_DEFENDANT_RESPONSE_TEMPLATE
        = "online-dq-with-no-Mediation-defendant-response";
    protected static final String ONLINE_DQ_WITH_NO_MEDIATION_CLAIMANT_RESPONSE_TEMPLATE
        = "online-dq-with-no-Mediation-claimant-response";
    protected static final String CLAIMANT_CCJ_REQUESTED_TEMPLATE = "claimantCcjRequested";
    protected static final String CLAIMANT_CCJ_REMINDER_TEMPLATE = "claimantCcjReminder";
    protected static final String CLAIMANT_SAYS_DEFENDANT_PAID_IN_FULL_TEMPLATE = "claimant-says-paid-in-full";
    protected static final String DEFENDANT_RESPOND_BY_ADMISSION = "defenceResponseByAdmissions";

    protected static final String FRONTEND_BASE_URL = "http://some.host.dot.com";
    protected static final String RESPOND_TO_CLAIM_URL = "http://some.host.dot.com/first-contact/start";
    protected static final String USER_EMAIL = "user@example.com";
    protected static final String USER_FULLNAME = "Steven Patrick";

    protected final Claim claim = SampleClaim.getDefault().toBuilder().respondedAt(LocalDateTime.now()).build();

    protected final Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Mock
    protected NotificationClient notificationClient;
    @Mock
    protected NotificationsProperties properties;
    @Mock
    protected NotificationTemplates templates;
    @Mock
    protected EmailTemplates emailTemplates;
    @Mock
    protected Appender<ILoggingEvent> mockAppender;

    @Captor
    protected ArgumentCaptor<LoggingEvent> captorLoggingEvent;
    @Mock
    protected AppInsights appInsights;

    @Captor
    protected ArgumentCaptor<Map<String, String>> templateParameters;

    @Before
    public void setUp() {
        log.addAppender(mockAppender);
    }

    @After
    public void tearDown() {
        log.detachAppender(mockAppender);
    }

    protected void assertWasLogged(CharSequence text) {
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getFormattedMessage()).contains(text);
    }

    protected void assertWasNotLogged(CharSequence text) {
        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getFormattedMessage()).doesNotContain(text);
    }
}

package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.PaidInFullJsonMapper;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class PaidInFullNotificationServiceTest {
    private static final String PAID_IN_FULL_EMAIL_ADDRESS = "test@example.com";
    private static final String SENDER_EMAIL = "sender@example.com";

    private PaidInFullNotificationService service;

    @Mock
    private EmailService emailService;
    @Mock
    private EmailProperties emailProperties;

    private final Claim claim = SampleClaim.builder()
        .withMoneyReceivedOn(LocalDate.now()).build();

    @Before
    public void setup() {
        when(emailProperties.getPaidInFullRecipient()).thenReturn(PAID_IN_FULL_EMAIL_ADDRESS);
        when(emailProperties.getSender()).thenReturn(SENDER_EMAIL);

        service = new PaidInFullNotificationService(
            emailService,
            emailProperties,
            new PaidInFullJsonMapper()
        );
    }

    @Test
    public void sendNotificationsSendsNotificationsToStaff() {
        service.notifyRobotics(new PaidInFullEvent(claim));

        ArgumentCaptor<EmailData> emailDataArgumentCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(emailService, once()).sendEmail(
            eq(SENDER_EMAIL),
            emailDataArgumentCaptor.capture()
        );

        assertEquals(PAID_IN_FULL_EMAIL_ADDRESS, emailDataArgumentCaptor.getValue().getTo());
        assertEquals("J paid in full " + claim.getReferenceNumber(), emailDataArgumentCaptor.getValue().getSubject());
        assertEquals("", emailDataArgumentCaptor.getValue().getMessage());
        assertFalse(emailDataArgumentCaptor.getValue().getAttachments().isEmpty());
    }
}

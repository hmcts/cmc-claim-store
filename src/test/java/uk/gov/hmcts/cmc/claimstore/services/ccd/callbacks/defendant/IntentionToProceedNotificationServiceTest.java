package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.defendant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;

@RunWith(MockitoJUnitRunner.class)
public class IntentionToProceedNotificationServiceTest {

    @Mock
    private StaffEmailProperties staffEmailProperties;

    @Mock
    private IntentionToProceedContentProvider emailContentProvider;

    @Mock
    private EmailService emailService;

    private IntentionToProceedNotificationService intentionToProceedNotificationService;

    private final String sender = "sender";

    @Before
    public void setUp() {
        when(emailContentProvider.createContent(anyMap())).thenReturn(new EmailContent("", ""));
        when(staffEmailProperties.getSender()).thenReturn(sender);
        intentionToProceedNotificationService = new IntentionToProceedNotificationService(staffEmailProperties,
            emailContentProvider, emailService);
    }

    @Test
    public void shouldSendNotificationIfOfflineDQs() {
        Claim claim = SampleClaim.builder()
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();
        intentionToProceedNotificationService.notifyCaseworkers(claim);

        verify(emailService, once()).sendEmail(eq(sender), any());
    }

    @Test
    public void shouldNotSendNotificationIfOnlineDQs() {
        Claim claim = SampleClaim.builder()
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .withFeatures(Collections.singletonList(DQ_FLAG.getValue()))
            .build();

        intentionToProceedNotificationService.notifyCaseworkers(claim);

        verify(emailService, never()).sendEmail(anyString(), any());
    }
}

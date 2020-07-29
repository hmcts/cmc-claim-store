package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedStaffNotificationServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private ClaimIssuedStaffNotificationEmailContentProvider provider;

    private List<PDF> documents;

    @Before
    public void setUp() {
        PDF sealedClaim = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
        PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
        documents = ImmutableList.of(sealedClaim, pinLetterClaim);
    }

    @Test
    public void notifyStaffOfClaimIssueWhenStaffEmailsEnabled() {
        //given
        when(staffEmailProperties.getRecipient()).thenReturn("some recipient");
        when(staffEmailProperties.getSender()).thenReturn("sender@mail.com");
        when(provider.createContent(anyMap())).thenReturn(new EmailContent("subject", "body"));

        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService
            = new ClaimIssuedStaffNotificationService(emailService, staffEmailProperties, provider, true, true);

        //when
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(SampleClaim.getDefault(), documents);

        //verify
        verify(provider).createContent(anyMap());
        verify(emailService).sendEmail(anyString(), any(EmailData.class));
    }

    @Test
    public void shouldNotNotifyStaffOfClaimIssueWhenStaffEmailsDisabled() {
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService
            = new ClaimIssuedStaffNotificationService(emailService, staffEmailProperties, provider, false, false);

        //when
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(SampleClaim.getDefault(), documents);

        //verify
        verify(provider, never()).createContent(anyMap());
        verify(emailService, never()).sendEmail(anyString(), any(EmailData.class));
    }
}

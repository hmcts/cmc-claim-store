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
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedStaffNotificationServiceTest {

    private Claim claim;
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Mock
    private EmailService emailService;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private ClaimIssuedStaffNotificationEmailContentProvider provider;
    private List<PDF> documents;

    @Before
    public void before() {
        claim = SampleClaim.getDefault();

        PDF sealedClaim = new PDF("0000-claim", "test".getBytes(), SEALED_CLAIM);
        PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
        documents = ImmutableList.of(sealedClaim, pinLetterClaim);

        claimIssuedStaffNotificationService
            = new ClaimIssuedStaffNotificationService(emailService, staffEmailProperties, provider);

        when(staffEmailProperties.getRecipient()).thenReturn("some recipient");
        when(provider.createContent(anyMap())).thenReturn(new EmailContent("subject", "body"));
    }

    @Test
    public void notifyStaffOfClaimIssue() {

        //when
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(claim, documents);

        //verify
        verify(staffEmailProperties).getRecipient();
        verify(provider).createContent(anyMap());
        verify(emailService).sendEmail(anyString(), any());
    }
}

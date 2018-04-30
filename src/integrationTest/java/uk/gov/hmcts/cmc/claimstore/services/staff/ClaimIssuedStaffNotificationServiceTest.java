package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;


public class ClaimIssuedStaffNotificationServiceTest extends MockSpringTest {

    @Autowired
    private ClaimIssuedStaffNotificationService service;
    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;
    @Autowired
    private StaffEmailProperties emailProperties;

    private Claim claim;


    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffOfClaimIssue(null);
    }



    @Test
    public void claimIssuedEmailShouldBeSentToStaff() {
        /*
        Mock Claim
        Use notifyStaffOfClaimIssue method
        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
         */

    }

    @Test
    public void claimIssuedEmailShouldNotBeSentToStaff() throws Exception {
    }
}

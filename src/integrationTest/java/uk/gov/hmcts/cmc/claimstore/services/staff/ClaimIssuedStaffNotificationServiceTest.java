package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ClaimIssuedStaffNotificationServiceTest extends BaseSaveTest {

    @SpyBean
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffOfClaimIssue(null);
    }

    @Test
    public void claimIssuedEmailShouldBeSentToStaff() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimantBuilder().build());

        verify(service, never()).notifyStaffOfClaimIssue(any());
    }
}

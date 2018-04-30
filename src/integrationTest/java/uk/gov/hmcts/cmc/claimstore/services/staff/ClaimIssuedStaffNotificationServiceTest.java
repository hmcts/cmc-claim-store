package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "feature_toggles.emailToStaff=true"
    }
)
public class ClaimIssuedStaffNotificationServiceTest extends BaseSaveTest {

    @SpyBean
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffOfClaimIssue(null);
    }

    @Test
    public void claimIssuedEmailShouldBeSentToStaffWhenSendToStaffIsTrue() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimantBuilder()
            .build()).andExpect(status().isOk());

        verify(service, times(1)).notifyStaffOfClaimIssue(any());
    }

    @Test
    public void claimIssuedEmailShouldNotBeSentToStaffWhenSendToStaffIsFalse() throws Exception {
        makeRequest(SampleClaimData.submittedByClaimantBuilder()
            .build());

        verify(service, never()).notifyStaffOfClaimIssue(any());
    }
}

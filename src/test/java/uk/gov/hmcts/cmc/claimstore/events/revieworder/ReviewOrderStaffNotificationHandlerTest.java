package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.ReviewOrderStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ReviewOrderStaffNotificationHandlerTest {

    private static final ReviewOrderEvent event = new ReviewOrderEvent(SampleClaim.getDefault());

    private ReviewOrderStaffNotificationHandler handler;

    @Mock
    ReviewOrderStaffNotificationService reviewOrderStaffNotificationService;

    @Before
    public void setup() {
        handler = new ReviewOrderStaffNotificationHandler(reviewOrderStaffNotificationService);
    }

    @Test
    public void notifyStaffForReviewOrder() {
        handler.onReviewOrderEvent(event);

        verify(reviewOrderStaffNotificationService, once()).notifyForReviewOrder(eq(event.getClaim()));
    }
}

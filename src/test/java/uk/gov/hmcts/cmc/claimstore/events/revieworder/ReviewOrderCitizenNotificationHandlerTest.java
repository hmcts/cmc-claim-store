package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.BaseNotificationServiceTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.DEFENDANT;
import static uk.gov.hmcts.cmc.domain.utils.EmailUtils.getDefendantEmail;

@RunWith(MockitoJUnitRunner.class)
public class ReviewOrderCitizenNotificationHandlerTest extends BaseNotificationServiceTest {

    public static final String NOTIFY_TO_DEFENDANT = "Notify to defendant when claimant requests for review order";
    public static final String NOTIFY_TO_CLAIMANT = "Notify to claimant when defendant requests for review order";
    public static final String AUTHORISATION = "Bearer let me in";

    private ReviewOrderCitizenNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Before
    public void setUp() {
        when(properties.getTemplates()).thenReturn(templates);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getReviewOrderEmailToDefendant())
            .thenReturn(NOTIFY_TO_DEFENDANT);
        when(emailTemplates.getReviewOrderEmailToClaimant())
            .thenReturn(NOTIFY_TO_CLAIMANT);

        handler = new ReviewOrderCitizenNotificationHandler(notificationService, properties);
    }

    @Test
    public void sendNotificationToDefendantWhenReviewOrderIsRequestedByClaimant() {
        ReviewOrderEvent event = new ReviewOrderEvent(
            AUTHORISATION,
            SampleClaim.builder()
                .withReviewOrder(ReviewOrder.builder()
                    .requestedBy(CLAIMANT)
                    .requestedAt(now())
                    .build()
                ).build());

        handler.onReviewOrderEvent(event);

        verify(notificationService, once()).sendMail(
            eq(getDefendantEmail(claim).orElse(null)),
            eq(NOTIFY_TO_DEFENDANT),
            anyMap(),
            eq(referenceForDefendant(event.getClaim().getReferenceNumber()))
        );
    }

    @Test
    public void sendNotificationToClaimantWhenReviewOrderIsRequestedByDefendant() {
        ReviewOrderEvent event = new ReviewOrderEvent(
            AUTHORISATION,
            SampleClaim.builder()
                .withReviewOrder(ReviewOrder.builder()
                    .requestedBy(DEFENDANT)
                    .requestedAt(now())
                    .build()
                ).build());

        handler.onReviewOrderEvent(event);

        verify(notificationService, once()).sendMail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(NOTIFY_TO_CLAIMANT),
            anyMap(),
            eq(referenceForDefendant(event.getClaim().getReferenceNumber()))
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendThrowIllegalArgumentExceptionWhenClaimHasNoReviewOrder() {
        ReviewOrderEvent event = new ReviewOrderEvent(
            AUTHORISATION,
            SampleClaim.builder().withReviewOrder(null).build());

        handler.onReviewOrderEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendThrowIllegalArgumentExceptionWhenRequestedByIsNullInReviewOrder() {
        ReviewOrderEvent event = new ReviewOrderEvent(
            AUTHORISATION,
            SampleClaim.builder()
                .withReviewOrder(ReviewOrder.builder()
                    .requestedBy(null)
                    .requestedAt(now())
                    .build()
                ).build());

        handler.onReviewOrderEvent(event);
    }
}

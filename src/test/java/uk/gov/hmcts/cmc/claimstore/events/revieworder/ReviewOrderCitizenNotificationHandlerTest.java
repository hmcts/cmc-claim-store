package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.notifications.BaseNotificationServiceTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.DEFENDANT;
import static uk.gov.hmcts.cmc.domain.utils.EmailUtils.getDefendantEmail;

@ExtendWith(MockitoExtension.class)
public class ReviewOrderCitizenNotificationHandlerTest extends BaseNotificationServiceTest {

    public static final String NOTIFY_TO_DEFENDANT = "Notify to defendant when claimant requests for review order";
    public static final String NOTIFY_TO_CLAIMANT = "Notify to claimant when defendant requests for review order";
    public static final String AUTHORISATION = "Bearer let me in";

    private ReviewOrderCitizenNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        lenient().when(properties.getTemplates()).thenReturn(templates);
        lenient().when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        lenient().when(templates.getEmail()).thenReturn(emailTemplates);
        lenient().when(emailTemplates.getReviewOrderEmailToDefendant())
            .thenReturn(NOTIFY_TO_DEFENDANT);
        lenient().when(emailTemplates.getReviewOrderEmailToClaimant())
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

    @Test
    public void sendThrowIllegalArgumentExceptionWhenClaimHasNoReviewOrder() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReviewOrderEvent event = new ReviewOrderEvent(
                AUTHORISATION,
                SampleClaim.builder().withReviewOrder(null).build());

            handler.onReviewOrderEvent(event);
        });
    }

    @Test
    public void sendThrowIllegalArgumentExceptionWhenRequestedByIsNullInReviewOrder() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReviewOrderEvent event = new ReviewOrderEvent(
                AUTHORISATION,
                SampleClaim.builder()
                    .withReviewOrder(ReviewOrder.builder()
                        .requestedBy(null)
                        .requestedAt(now())
                        .build()
                    ).build());

            handler.onReviewOrderEvent(event);
        });
    }
}

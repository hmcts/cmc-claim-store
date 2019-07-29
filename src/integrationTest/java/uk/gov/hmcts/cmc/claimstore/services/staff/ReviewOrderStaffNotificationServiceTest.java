package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;

public class ReviewOrderStaffNotificationServiceTest extends MockSpringTest {

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private ReviewOrderStaffNotificationService service;

    @Test
    public void shouldSendEmailWithExpectedContentsForReviewOrder() {
        Claim claim = SampleClaim.builder()
            .withReviewOrder(ReviewOrder.builder()
                .reason("My reason")
                .requestedAt(now())
                .requestedBy(CLAIMANT)
                .build())
            .build();

        service.notifyForReviewOrder(claim);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualTo(format("%s: %s v %s reconsideration request",
                claim.getReferenceNumber(),
                claim.getClaimData().getClaimant().getName(),
                claim.getClaimData().getDefendant().getName()));

        assertThat(emailDataArgument.getValue().getMessage())
            .contains(format("%s has requested a review of the Directions Order, please see their comments below",
                claim.getClaimData().getClaimant().getName()));
    }
}

package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationReferenceBuilderTest {

    @Test
    public void shouldGenerateValidReferenceForClaimantResponseIssuedNotification() {
        assertThat(NotificationReferenceBuilder.ResponseSubmitted.referenceForClaimant("111"))
            .isEqualTo("claimant-response-notification-111");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantResponseIssuedNotification() {
        assertThat(NotificationReferenceBuilder.ResponseSubmitted.referenceForDefendant("222"))
            .isEqualTo("defendant-response-notification-222");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantMoreTimeRequestedNotification() {
        assertThat(NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant("abc"))
            .isEqualTo("more-time-requested-notification-to-defendant-abc");
    }

}

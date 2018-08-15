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

    @Test
    public void shouldGenerateValidReferenceForClaimantCCJRequestedNotification() {
        assertThat(NotificationReferenceBuilder.CCJRequested.referenceForClaimant("abc"))
            .isEqualTo("claimant-ccj-requested-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForClaimantOfferMadeNotification() {
        assertThat(NotificationReferenceBuilder.OfferMade.referenceForClaimant("abc"))
            .isEqualTo("claimant-offer-made-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantOfferMadeNotification() {
        assertThat(NotificationReferenceBuilder.OfferMade.referenceForDefendant("abc"))
            .isEqualTo("defendant-offer-made-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForClaimantAcceptedMadeNotification() {
        assertThat(NotificationReferenceBuilder.OfferAccepted.referenceForClaimant("abc"))
            .isEqualTo("to-claimant-offer-accepted-by-claimant-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantOfferAcceptedNotification() {
        assertThat(NotificationReferenceBuilder.OfferAccepted.referenceForDefendant("abc"))
            .isEqualTo("to-defendant-offer-accepted-by-claimant-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForClaimantOfferRejectedNotification() {
        assertThat(NotificationReferenceBuilder.OfferRejected.referenceForClaimant("abc"))
            .isEqualTo("to-claimant-offer-rejected-by-claimant-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantOfferRejectedNotification() {
        assertThat(NotificationReferenceBuilder.OfferRejected.referenceForDefendant("abc"))
            .isEqualTo("to-defendant-offer-rejected-by-claimant-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForDefendantSignSettlementAgreementNotification() {
        assertThat(
            NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForDefendant("abc")
        ).isEqualTo("to-defendant-claimant’s-response-submitted-notification-abc");
    }

    @Test
    public void shouldGenerateValidReferenceForClaimantSignSettlementAgreementNotification() {
        assertThat(
            NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForClaimant("abc")
        ).isEqualTo("to-claimant-claimant’s-response-submitted-notification-abc");
    }
}

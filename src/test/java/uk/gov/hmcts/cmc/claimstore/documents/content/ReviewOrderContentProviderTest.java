package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ReviewOrderContentProviderTest {

    private final ReviewOrderContentProvider provider = new ReviewOrderContentProvider();

    @Test
    public void shouldProvideReviewOrderDataForClaimant() {
        LocalDateTime now = LocalDateTime.parse("2020-11-16T10:11:30");
        Claim claim = SampleClaim.builder()
            .withReviewOrder(ReviewOrder.builder()
                .reason("just because")
                .requestedAt(now)
                .requestedBy(ReviewOrder.RequestedBy.CLAIMANT)
                .build())
            .build();
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).contains(
            entry("claimReferenceNumber", claim.getReferenceNumber()),
            entry("reviewDate", "16 November 2020"),
            entry("reviewReason", "just because"),
            entry("partyFullName", claim.getClaimData().getClaimant().getName()),
            entry("partyAddress", claim.getClaimData().getClaimant().getAddress())
        );
    }

    @Test
    public void shouldProvideReviewOrderDataForDefendant() {
        LocalDateTime now = LocalDateTime.parse("2020-11-16T10:11:30");
        Claim claim = SampleClaim.builder()
            .withReviewOrder(ReviewOrder.builder()
                .reason("just because")
                .requestedAt(now)
                .requestedBy(ReviewOrder.RequestedBy.DEFENDANT)
                .build())
            .build();
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).contains(
            entry("claimReferenceNumber", claim.getReferenceNumber()),
            entry("reviewDate", "16 November 2020"),
            entry("reviewReason", "just because"),
            entry("partyFullName", claim.getClaimData().getDefendant().getName()),
            entry("partyAddress", claim.getClaimData().getDefendant().getAddress())
        );
    }

    @Test
    public void shouldDefaultReasonIfNotProvided() {
        LocalDateTime now = LocalDateTime.parse("2020-11-16T10:11:30");
        Claim claim = SampleClaim.builder()
            .withReviewOrder(ReviewOrder.builder()
                .reason(null)
                .requestedAt(now)
                .requestedBy(ReviewOrder.RequestedBy.DEFENDANT)
                .build())
            .build();
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).contains(
            entry("reviewReason", "none provided")
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenClaimDoesNotHaveReviewOrder() {
        Claim claim = SampleClaim.builder()
            .withReviewOrder(null)
            .build();
        provider.createContent(claim);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenGivenNullClaim() {
        //noinspection ConstantConditions
        provider.createContent(null);
    }
}

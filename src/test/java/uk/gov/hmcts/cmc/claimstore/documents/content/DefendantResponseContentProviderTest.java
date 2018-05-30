package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantResponseContentProviderTest {

    private Claim claim = SampleClaim.getWithDefaultResponse();

    private DefendantResponseContentProvider provider = new DefendantResponseContentProvider(
        new PartyDetailsContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        ),
        new NotificationsProperties()
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideClaimReferenceNumber() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getReferenceNumber())
            .isEqualTo(claim.getReferenceNumber());

    }

    @Test
    public void shouldProvideClaimSubmittedOn() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getIssuedOn())
            .isEqualTo(formatDate(claim.getIssuedOn()));
    }

    @Test
    public void shouldProvideClaimantFullName() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(((ClaimContent) content.get("claim")).getSignerName())
            .isEqualTo(claim.getClaimData().getClaimant().getName());
    }

    @Test
    public void shouldProvideDefendantDetails() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("defendant");
        assertThat(content.get("defendant")).isInstanceOf(PartyDetailsContent.class);
    }

    @Test
    public void shouldProvideResponseDefence() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsEntry("responseDefence",
                ((FullDefenceResponse) claim.getResponse().orElseThrow(IllegalStateException::new))
                    .getDefence().orElse(null)
            );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvidePaymentDeclaration() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("paymentDeclaration");
        assertThat((Map<String, String>) content.get("paymentDeclaration"))
            .containsOnlyKeys("paidDate", "explanation")
            .containsValues("2 January 2016", "Paid cash");
    }

    @Test
    public void shouldProvideResponseTimeline() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsKeys("timelineComment", "events");
    }
}

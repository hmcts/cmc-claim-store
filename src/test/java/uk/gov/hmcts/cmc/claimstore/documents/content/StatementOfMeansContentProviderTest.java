package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class StatementOfMeansContentProviderTest {

    private Claim claim = SampleClaim.getWithFullAdmissionResponse();

    private StatementOfMeansContentProvider provider = new StatementOfMeansContentProvider(
        new PartyDetailsContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        ),
        new NotificationsProperties()
    );
    private StatementOfMeans statementOfMeans = StatementOfMeans.builder().build();

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
    public void shouldProvideResponseTimeline() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsKeys("bankAccounts", "debts");
    }
}

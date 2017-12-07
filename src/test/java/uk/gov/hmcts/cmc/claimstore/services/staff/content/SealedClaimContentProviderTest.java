package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SealedClaimContentProviderTest {

    private static final String EMAIL = "address@domain.com";

    private Claim claim = SampleClaim.getDefault();

    private SealedClaimContentProvider provider = new SealedClaimContentProvider(
        new ClaimantContentProvider(
            new PersonContentProvider()
        ),
        new PersonContentProvider(),
        new ClaimContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        )
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaim() {
        provider.createContent(null, EMAIL);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullEmail() {
        provider.createContent(claim, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentForEmptyEmail() {
        provider.createContent(claim, "");
    }

    @Test
    public void shouldProvideClaimantContent() {
        Map<String, Object> content = provider.createContent(claim, EMAIL);

        assertThat(content).containsKey("claimant");
        assertThat(content.get("claimant")).isInstanceOf(ClaimantContent.class);
    }

    @Test
    public void shouldProvideDefendantContent() {
        Map<String, Object> content = provider.createContent(claim, EMAIL);

        assertThat(content).containsKey("defendant");
        assertThat(content.get("defendant")).isInstanceOf(PersonContent.class);
    }

    @Test
    public void shouldProvideClaimContent() {
        Map<String, Object> content = provider.createContent(claim, EMAIL);

        assertThat(content).containsKey("claim");
        assertThat(content.get("claim")).isInstanceOf(ClaimContent.class);
    }

}

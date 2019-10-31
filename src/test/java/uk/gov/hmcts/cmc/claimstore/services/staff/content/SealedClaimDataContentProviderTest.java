package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SealedClaimDataContentProviderTest {

    private Claim claim = SampleClaim.getDefault();

    private ClaimContentProvider provider = new ClaimContentProvider(
        new ClaimantContentProvider(
            new PersonContentProvider()
        ),
        new PersonContentProvider(),
        new ClaimDataContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemDefaultZone())
            )
        )
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideClaimantContent() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("claimant");
        assertThat(content.get("claimant")).isInstanceOf(ClaimantContent.class);
    }

    @Test
    public void shouldProvideDefendantContent() {
        Map<String, Object> content = provider.createContent(claim);
        PersonContent defendantContent = (PersonContent)content.get("defendant");

        assertThat(content).containsKey("defendant");
        Assert.assertEquals("0776655443322", defendantContent.getPhoneNumber());
    }

    @Test
    public void shouldProvideClaimContent() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsKey("claim");
        assertThat(content.get("claim")).isInstanceOf(ClaimContent.class);
    }

}

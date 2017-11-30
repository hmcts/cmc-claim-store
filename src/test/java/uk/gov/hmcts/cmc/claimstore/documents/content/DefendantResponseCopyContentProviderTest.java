package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantResponseCopyContentProviderTest {

    private Claim claim = SampleClaim.getWithDefaultResponse();

    private DefendantResponseCopyContentProvider provider = new DefendantResponseCopyContentProvider(
        new PartyDetailsContentProvider(),
        new ClaimContentProvider(new InterestContentProvider(new InterestCalculationService(Clock.systemDefaultZone()))
        ));

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideClaimReferenceNumber() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsEntry("claimReferenceNumber", claim.getReferenceNumber());
    }

    @Test
    public void shouldProvideClaimSubmittedOn() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsEntry("claimSubmittedOn", formatDate(claim.getCreatedAt()));
    }

    @Test
    public void shouldProvideClaimantFullName() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content).containsEntry(
            "claimantFullName",
            claim.getClaimData().getClaimant().getName()
        );
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
            .containsEntry("responseDefence", claim.getResponse().orElseThrow(IllegalStateException::new).getDefence());
    }

}

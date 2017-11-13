package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleClaim;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantResponseCopyContentProviderTest {

    private Claim claim = SampleClaim.getWithDefaultResponse();

    private DefendantResponseCopyContentProvider provider = new DefendantResponseCopyContentProvider(
        new DefendantDetailsContentProvider()
    );

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
        assertThat(content.get("defendant")).isInstanceOf(DefendantDetailsContent.class);
    }

    @Test
    public void shouldProvideResponseDefence() {
        Map<String, Object> content = provider.createContent(claim);

        assertThat(content)
            .containsEntry("responseDefence", claim.getResponse().orElseThrow(IllegalStateException::new).getDefence());
    }

}

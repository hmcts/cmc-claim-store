package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantResponseCopyContentProviderTest {

    private Claim claim = SampleClaim.getDefault();
    private DefendantResponse defendantResponse = SampleDefendantResponse.getDefault();

    private DefendantResponseCopyContentProvider provider = new DefendantResponseCopyContentProvider(
        new DefendantDetailsContentProvider()
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null, defendantResponse);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullResponse() {
        provider.createContent(claim, null);
    }

    @Test
    public void shouldProvideClaimReferenceNumber() {
        Map<String, Object> content = provider.createContent(claim, defendantResponse);

        assertThat(content).containsEntry("claimReferenceNumber", claim.getReferenceNumber());
    }

    @Test
    public void shouldProvideClaimSubmittedOn() {
        Map<String, Object> content = provider.createContent(claim, defendantResponse);

        assertThat(content).containsEntry("claimSubmittedOn", formatDate(claim.getCreatedAt()));
    }

    @Test
    public void shouldProvideClaimantFullName() {
        Map<String, Object> content = provider.createContent(claim, defendantResponse);

        assertThat(content).containsEntry(
            "claimantFullName",
            claim.getClaimData().getClaimant().getName()
        );
    }

    @Test
    public void shouldProvideDefendantDetails() {
        Map<String, Object> content = provider.createContent(claim, defendantResponse);

        assertThat(content).containsKey("defendant");
        assertThat(content.get("defendant")).isInstanceOf(DefendantDetailsContent.class);
    }

    @Test
    public void shouldProvideResponseDefence() {
        Map<String, Object> content = provider.createContent(claim, defendantResponse);

        assertThat(content).containsEntry("responseDefence", defendantResponse.getResponse().getDefence());
    }

}

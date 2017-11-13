package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantContentProviderTest {

    private static final String EMAIL = "claimant@domain.com";

    private Individual claimant = SampleParty.builder().individual();

    private ClaimantContentProvider provider = new ClaimantContentProvider(
        new PersonContentProvider()
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaimant() {
        provider.createContent(null, EMAIL);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullEmail() {
        provider.createContent(claimant, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentForEmptyEmail() {
        provider.createContent(claimant, "");
    }

    @Test
    public void shouldProvideExpectedEmail() {
        ClaimantContent content = provider.createContent(claimant, EMAIL);

        assertThat(content.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    public void shouldProvideAFullName() {
        ClaimantContent content = provider.createContent(claimant, EMAIL);

        assertThat(content.getFullName()).isNotEmpty();
    }

    @Test
    public void shouldProvideAnAddress() {
        ClaimantContent content = provider.createContent(claimant, EMAIL);

        assertThat(content.getAddress()).isNotNull();
    }

}

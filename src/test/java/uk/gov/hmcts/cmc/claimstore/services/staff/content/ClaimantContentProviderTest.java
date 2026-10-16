package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimantContent;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class ClaimantContentProviderTest {

    private static final String EMAIL = "claimant@domain.com";

    private final Individual claimant = SampleParty.builder().individual();

    private final ClaimantContentProvider provider = new ClaimantContentProvider(
        new PersonContentProvider()
    );

    @Test
    public void shouldThrowNullPointerForNullClaimant() {
        assertThrows(NullPointerException.class, () -> {
            provider.createContent(null, EMAIL);
        });
    }

    @Test
    public void shouldThrowNullPointerForNullEmail() {
        assertThrows(NullPointerException.class, () -> {
            provider.createContent(claimant, null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentForEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            provider.createContent(claimant, "");
        });
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

    @Test
    public void shouldProvideDateOfBirth() {
        ClaimantContent content = provider.createContent(claimant, EMAIL);

        assertThat(content.getDateOfBirth()).isEqualTo(formatDate(claimant.getDateOfBirth()));

    }

    @Test
    public void shouldProvidePhoneNumber() {
        ClaimantContent content = provider.createContent(claimant, EMAIL);

        assertThat(content.getPhoneNumber()).isNotNull();
    }

}

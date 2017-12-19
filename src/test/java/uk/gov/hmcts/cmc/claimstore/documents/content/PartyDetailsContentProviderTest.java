package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.PartyDetailsContent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;

public class PartyDetailsContentProviderTest {

    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(2017, 7, 19);

    private final PartyDetailsContentProvider provider = new PartyDetailsContentProvider();

    private final TheirDetails defendant = SampleTheirDetails.builder()
        .individualDetails();

    private final Address correspondenceAddress = SampleAddress.builder()
        .withLine1("Correspondence Road 1")
        .withPostcode("BB 127NQ")
        .build();

    private Individual notAmendedDetails() {
        return SampleParty.builder()
            .withName(defendant.getName())
            .withAddress(defendant.getAddress())
            .withCorrespondenceAddress(correspondenceAddress)
            .withDateOfBirth(
                DATE_OF_BIRTH
            )
            .individual();
    }

    private Individual amendedDetails() {
        return SampleParty.builder()
            .withName("John Doe")
            .withAddress(
                SampleAddress.builder()
                    .withLine1("Somewhere")
                    .withCity("Manchester")
                    .withPostcode("BB12 7NQ")
                    .build()
            )
            .withDateOfBirth(
                DATE_OF_BIRTH
            )
            .individual();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendant() {
        provider.createContent(null, amendedDetails(), null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantDetails() {
        provider.createContent(defendant, null, null);
    }

    @Test
    public void nameShouldBeAsGivenByClaimantWhenNotAmended() {
        PartyDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getNameAmended()).isFalse();
        assertThat(content.getFullName()).isEqualTo(defendant.getName());
    }

    @Test
    public void nameShouldBeAsGivenByDefendantWhenAmended() {
        Individual party = amendedDetails();
        PartyDetailsContent content = provider.createContent(defendant, party, DEFENDANT_EMAIL);

        assertThat(content.getNameAmended()).isTrue();
        assertThat(content.getFullName()).isEqualTo(party.getName());
    }

    @Test
    public void addressShouldBeAsGivenByClaimantWhenNotAmended() {
        PartyDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getAddressAmended()).isFalse();
        assertThat(content.getAddress()).isEqualTo(defendant.getAddress());
    }

    @Test
    public void addressShouldBeAsGivenByDefendantWhenAmended() {
        Individual party = amendedDetails();
        PartyDetailsContent content = provider.createContent(defendant, party, DEFENDANT_EMAIL);

        assertThat(content.getAddressAmended()).isTrue();
        assertThat(content.getAddress()).isEqualTo(party.getAddress());
    }

    @Test
    public void shouldProvideDateOfBirth() {
        PartyDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getDateOfBirth()).isEqualTo(formatDate(DATE_OF_BIRTH));
    }

    @Test
    public void shouldProvideCorrespondenceAddress() {
        PartyDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getCorrespondenceAddress()).isEqualTo(correspondenceAddress);
    }

}

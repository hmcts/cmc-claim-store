package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;

public class DefendantDetailsContentProviderTest {

    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(2017, 7, 19);

    private TheirDetails defendant = SampleTheirDetails.builder()
        .individualDetails();

    private Address correspondenceAddress = SampleAddress.builder()
        .withLine1("Correspondence Road 1")
        .withPostcode("BB 127NQ")
        .build();

    private Response notAmendedDetails() {
        return SampleResponse.builder().withDefendantDetails(
            SampleParty.builder()
                .withName(defendant.getName())
                .withAddress(defendant.getAddress())
                .withCorrespondenceAddress(correspondenceAddress)
                .withDateOfBirth(
                    DATE_OF_BIRTH
                )
                .individual()
        ).build();
    }

    private Response amendedDetails() {
        return SampleResponse.builder().withDefendantDetails(
            SampleParty.builder()
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
                .individual()
        ).build();
    }

    private DefendantDetailsContentProvider provider = new DefendantDetailsContentProvider();

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
        DefendantDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getNameAmended()).isFalse();
        assertThat(content.getFullName()).isEqualTo(defendant.getName());
    }

    @Test
    public void nameShouldBeAsGivenByDefendantWhenAmended() {
        Response defendantResponse = amendedDetails();
        DefendantDetailsContent content = provider.createContent(defendant, defendantResponse, DEFENDANT_EMAIL);

        assertThat(content.getNameAmended()).isTrue();
        assertThat(content.getFullName()).isEqualTo(
            defendantResponse
                .getDefendant()
                .getName()
        );
    }

    @Test
    public void addressShouldBeAsGivenByClaimantWhenNotAmended() {
        DefendantDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getAddressAmended()).isFalse();
        assertThat(content.getAddress()).isEqualTo(defendant.getAddress());
    }

    @Test
    public void addressShouldBeAsGivenByDefendantWhenAmended() {
        Response defendantResponse = amendedDetails();
        DefendantDetailsContent content = provider.createContent(defendant, defendantResponse, DEFENDANT_EMAIL);

        assertThat(content.getAddressAmended()).isTrue();
        assertThat(content.getAddress()).isEqualTo(defendantResponse
            .getDefendant()
            .getAddress());
    }

    @Test
    public void shouldProvideDateOfBirth() {
        DefendantDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getDateOfBirth()).isEqualTo(formatDate(DATE_OF_BIRTH));
    }

    @Test
    public void shouldProvideCorrespondenceAddress() {
        DefendantDetailsContent content = provider.createContent(defendant, notAmendedDetails(), DEFENDANT_EMAIL);

        assertThat(content.getCorrespondenceAddress()).isEqualTo(correspondenceAddress);
    }

}

package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonContentProviderTest {

    private String partyType;
    private String name;
    private Address address;
    private Address correspondenceAddress;
    private String emailAddress;

    private PersonContentProvider provider = new PersonContentProvider();

    @Before
    public void beforeEachTest() {
        partyType = "individual";
        name = "John James Smith";
        address = SampleAddress.builder()
            .withLine1("28 Somewhere Homes")
            .withLine2("75 That Way Road")
            .withLine3("")
            .withCity("London")
            .withPostcode("AQ9 5FS")
            .build();
        correspondenceAddress = SampleAddress.builder()
            .withLine1("Correspondence Road")
            .withCity("Manchester")
            .withPostcode("CQ9 6FS")
            .build();
        emailAddress = "blah@blah.com";
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullName() {
        provider.createContent(
            partyType,
            null,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null
        );
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullAddress() {
        provider.createContent(
            partyType,
            name,
            null,
            correspondenceAddress,
            emailAddress,
            null,
            null
        );
    }

    @Test
    public void shouldProvideExpectedFullName() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            null,
            null,
            null
        );

        assertThat(content.getFullName()).isEqualTo(name);
    }

    @Test
    public void shouldProvideExpectedAddress() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null
        );

        assertThat(content.getAddress().getLine1()).isEqualTo(address.getLine1());
        assertThat(content.getAddress().getLine2()).isEqualTo(address.getLine2());
        assertThat(content.getAddress().getLine3()).isEqualTo(address.getLine3());
        assertThat(content.getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(content.getAddress().getPostcode()).isEqualTo(address.getPostcode());
    }

    @Test
    public void shouldProvideExpectedCorrespondenceAddress() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null
        );

        assertThat(content.getCorrespondenceAddress().getLine1()).isEqualTo(correspondenceAddress.getLine1());
        assertThat(content.getCorrespondenceAddress().getLine2()).isEqualTo(correspondenceAddress.getLine2());
        assertThat(content.getCorrespondenceAddress().getLine3()).isEqualTo(correspondenceAddress.getLine3());
        assertThat(content.getCorrespondenceAddress().getCity()).isEqualTo(correspondenceAddress.getCity());
        assertThat(content.getCorrespondenceAddress().getPostcode()).isEqualTo(correspondenceAddress.getPostcode());
    }

    @Test
    public void shouldProvideExpectedEmailAddress() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null
        );

        assertThat(content.getEmail()).isEqualTo(emailAddress);
    }

    @Test
    public void shouldAcceptNullEmailAddress() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            null,
            null,
            null
        );

        assertThat(content.getEmail()).isEqualTo(null);
    }
}

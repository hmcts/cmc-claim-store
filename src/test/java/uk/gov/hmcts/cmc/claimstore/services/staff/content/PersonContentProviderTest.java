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
    private String phoneNumber;
    private String dateOfBirth;

    private final PersonContentProvider provider = new PersonContentProvider();

    @Before
    public void beforeEachTest() {
        partyType = "individual";
        name = "John James Smith";
        address = SampleAddress.builder()
            .line1("28 Somewhere Homes")
            .line2("75 That Way Road")
            .line3("")
            .city("London")
            .postcode("AQ9 5FS")
            .build();
        correspondenceAddress = SampleAddress.builder()
            .line1("Correspondence Road")
            .city("Manchester")
            .postcode("CQ9 6FS")
            .build();
        emailAddress = "blah@blah.com";
        phoneNumber = "07786556746";
        dateOfBirth = "1 January 1987";
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
            null,
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
            null,
            phoneNumber,
            dateOfBirth
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
            null,
            phoneNumber,
            dateOfBirth
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
            null,
            phoneNumber,
            dateOfBirth
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
            null,
            phoneNumber,
            dateOfBirth
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
            null,
            phoneNumber,
            dateOfBirth
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
            null,
            phoneNumber,
            dateOfBirth
        );

        assertThat(content.getEmail()).isEqualTo(null);
    }

    @Test
    public void shouldProvideExpectedPhoneNumberAndDateOfBirth() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null,
            phoneNumber,
            dateOfBirth
        );

        assertThat(content.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(content.getDateOfBirth()).isEqualTo(dateOfBirth);
    }

    @Test
    public void shouldAcceptNullPhoneNumberAndDateOfBirth() {
        PersonContent content = provider.createContent(
            partyType,
            name,
            address,
            correspondenceAddress,
            emailAddress,
            null,
            null,
            null,
            null
        );

        assertThat(content.getDateOfBirth()).isEqualTo(null);
        assertThat(content.getPhoneNumber()).isEqualTo(null);
    }
}

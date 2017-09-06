package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonContentProviderTest {

    private String name;
    private Address address;
    private Address correspondenceAddress;
    private String emailAddress;

    private PersonContentProvider provider = new PersonContentProvider();

    @Before
    public void beforeEachTest() {
        name = "John James Smith";
        address = SampleAddress.builder()
            .withLine1("28 Somewhere Homes")
            .withLine2("75 That Way Road")
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
        provider.createContent(null, address, correspondenceAddress, emailAddress);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullAddress() {
        provider.createContent(name, null, correspondenceAddress, emailAddress);
    }

    @Test
    public void shouldProvideExpectedFullName() {
        PersonContent content = provider.createContent(name, address, correspondenceAddress, null);

        assertThat(content.getFullName()).isEqualTo(name);
    }

    @Test
    public void shouldProvideExpectedAddress() {
        PersonContent content = provider.createContent(name, address, correspondenceAddress, emailAddress);

        assertThat(content.getAddress().getLine1()).isEqualTo(address.getLine1());
        assertThat(content.getAddress().getLine2()).isEqualTo(address.getLine2());
        assertThat(content.getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(content.getAddress().getPostcode()).isEqualTo(address.getPostcode());
    }

    @Test
    public void shouldProvideExpectedCorrespondenceAddress() {
        PersonContent content = provider.createContent(name, address, correspondenceAddress, emailAddress);

        assertThat(content.getCorrespondenceAddress().getLine1()).isEqualTo(correspondenceAddress.getLine1());
        assertThat(content.getCorrespondenceAddress().getLine2()).isEqualTo(correspondenceAddress.getLine2());
        assertThat(content.getCorrespondenceAddress().getCity()).isEqualTo(correspondenceAddress.getCity());
        assertThat(content.getCorrespondenceAddress().getPostcode()).isEqualTo(correspondenceAddress.getPostcode());
    }

    @Test
    public void shouldProvideExpectedEmailAddress() {
        PersonContent content = provider.createContent(name, address, correspondenceAddress, emailAddress);

        assertThat(content.getEmail()).isEqualTo(emailAddress);
    }

    @Test
    public void shouldAcceptNullEmailAddress() {
        PersonContent content = provider.createContent(name, address, correspondenceAddress, null);

        assertThat(content.getEmail()).isEqualTo(null);
    }
}

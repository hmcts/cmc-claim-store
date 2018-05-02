package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class TheirDetailsTest {

    @Test
    public void shouldBeInvalidWhenGivenNullName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenTooLongName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(StringUtils.repeat("ha", 128))
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be longer than 255 characters");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullAddress() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withAddress(null)
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("address : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidAddress() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withAddress(SampleAddress.builder()
                .withPostcode("")
                .build())
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("address.postcode : Postcode is not of valid format");
    }

    @Test
    public void shouldBeValidWhenGivenNullEmail() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail(null)
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenValidEmail() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail("user@example.com")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyEmail() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail("")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("email : not a well-formed email address");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmailWithWhitespacesOnly() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail(" ")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("email : not a well-formed email address");
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidEmail() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail("this is not a valid email address")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("email : not a well-formed email address");
    }

    @Test
    public void shouldBeInvalidWhenGivenNonTrimmedEmail() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail(" user@example.com ")
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("email : not a well-formed email address");
    }

    @Test
    public void shouldBeValidWhenGivenNullServiceAddress() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withServiceAddress(null)
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenInvalidServiceAddress() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withServiceAddress(SampleAddress.builder()
                .withPostcode("")
                .build())
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("serviceAddress.postcode : Postcode is not of valid format");
    }
}

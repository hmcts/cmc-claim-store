package uk.gov.hmcts.cmc.domain.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

@ExtendWith(MockitoExtension.class)
class TheirDetailsTest {
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
                .postcode("")
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

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"this is not a valid email address",
        " ", "  user@example.com "})
    void shouldBeInvalidWhenGivenEmptyEmail(String input) {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withEmail(input)
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
                .postcode("")
                .build())
            .partyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("serviceAddress.postcode : Postcode is not of valid format");
    }
}

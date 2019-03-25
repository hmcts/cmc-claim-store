package uk.gov.hmcts.cmc.domain.models.otherparty;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class IndividualDetailsTest {

    @Test
    public void shouldBeValidWhenGivenNullTitle() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withTitle(null)
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenAgeInRange() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withDateOfBirth(LocalDate.now().minusYears(20))
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenAgeOutsideOfRange() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withDateOfBirth(LocalDate.now().plusYears(200))
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("dateOfBirth : Age must be between 18 and 150");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withFirstName(null)
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("firstName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withFirstName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("firstName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withLastName(null)
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("lastName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withLastName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("lastName : may not be empty");
    }
}

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
            .withName("")
            .withFirstName(null)
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withFirstName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withLastName(null)
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withLastName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }

    //todo ROC-5160  remove these tests once frontend is merged
    @Test
    public void shouldBeValidWhenGivenNameAndEmptyLastNameAndFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("a name")
            .withFirstName("")
            .withLastName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyNameAndValidLastNameAndFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withFirstName("some")
            .withLastName("some")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyNameAndEmptyLastNameAndFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withFirstName("")
            .withLastName("")
            .individualDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }
}

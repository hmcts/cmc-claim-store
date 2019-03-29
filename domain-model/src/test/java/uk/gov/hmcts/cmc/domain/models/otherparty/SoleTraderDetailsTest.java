package uk.gov.hmcts.cmc.domain.models.otherparty;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class SoleTraderDetailsTest {

    @Test
    public void shouldBeValidWhenGivenNullTitle() {
        TheirDetails soleTraderDetails = SampleTheirDetails.builder()
            .withTitle(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(soleTraderDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidWhenGivenNullFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .withFirstName(null)
            .soleTraderDetails();

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
            .soleTraderDetails();

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
            .soleTraderDetails();

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
            .soleTraderDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("Either name or both first and last name must be provided");
    }

    @Test
    public void shouldBeValidWhenGivenNullBusinessName() {
        TheirDetails soleTraderDetails = SampleTheirDetails.builder()
            .withBusinessName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(soleTraderDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyBusinessName() {
        TheirDetails soleTraderDetails = SampleTheirDetails.builder()
            .withBusinessName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(soleTraderDetails);

        assertThat(validationErrors).isEmpty();
    }

    //todo ROC-5160 remove these tests once frontend is merged
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

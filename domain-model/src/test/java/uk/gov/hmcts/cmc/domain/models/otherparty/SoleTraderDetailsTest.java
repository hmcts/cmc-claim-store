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
            .withName(null)
            .withFirstName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("firstName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyFirstName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .withFirstName("")
            .soleTraderDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("firstName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .withLastName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("lastName : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyLastName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .withLastName("")
            .soleTraderDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("lastName : may not be empty");
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

}

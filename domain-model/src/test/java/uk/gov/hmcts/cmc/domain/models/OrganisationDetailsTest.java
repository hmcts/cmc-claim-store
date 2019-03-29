package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class OrganisationDetailsTest {

    @Test
    public void shouldBeInvalidWhenGivenNullName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .organisationDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .organisationDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenTooLongName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(StringUtils.repeat("ha", 128))
            .organisationDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be longer than 255 characters");
    }

    @Test
    public void shouldBeValidWhenGivenNullContactPerson() {
        OrganisationDetails organisationDetails = SampleTheirDetails.builder()
            .withContactPerson(null)
            .organisationDetails();

        Set<String> validationErrors = validate(organisationDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyContactPerson() {
        OrganisationDetails organisationDetails = SampleTheirDetails.builder()
            .withContactPerson("")
            .organisationDetails();

        Set<String> validationErrors = validate(organisationDetails);

        assertThat(validationErrors).isEmpty();
    }

}

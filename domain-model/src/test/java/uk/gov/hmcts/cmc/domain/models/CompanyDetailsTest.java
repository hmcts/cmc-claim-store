package uk.gov.hmcts.cmc.domain.models;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class CompanyDetailsTest {

    @Test
    public void shouldBeInvalidWhenGivenNullName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(null)
            .companyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenEmptyName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName("")
            .companyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenGivenTooLongName() {
        TheirDetails theirDetails = SampleTheirDetails.builder()
            .withName(StringUtils.repeat("ha", 128))
            .companyDetails();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("name : may not be longer than 255 characters");
    }

    @Test
    public void shouldBeValidWhenGivenNullContactPerson() {
        CompanyDetails companyDetails = SampleTheirDetails.builder()
            .withContactPerson(null)
            .companyDetails();

        Set<String> validationErrors = validate(companyDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyContactPerson() {
        CompanyDetails companyDetails = SampleTheirDetails.builder()
            .withContactPerson("")
            .companyDetails();

        Set<String> validationErrors = validate(companyDetails);

        assertThat(validationErrors).isEmpty();
    }

}

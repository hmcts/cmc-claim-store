package uk.gov.hmcts.cmccase.models;

import org.junit.Test;
import uk.gov.hmcts.cmccase.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmccase.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmccase.utils.BeanValidator.validate;

public class CompanyDetailsTest {

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

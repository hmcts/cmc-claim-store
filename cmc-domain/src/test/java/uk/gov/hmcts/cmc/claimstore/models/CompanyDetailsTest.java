package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

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

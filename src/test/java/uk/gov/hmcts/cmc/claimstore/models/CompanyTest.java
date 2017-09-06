package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.party.Company;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class CompanyTest {

    @Test
    public void shouldReturnValidationErrorWhenContactPersonIsNotNull() {
        Company company = SampleParty.builder()
            .withContactPerson(null)
            .company();

        Set<String> validationErrors = validate(company);

        assertThat(validationErrors).hasSize(1).contains("contactPerson : may not be empty");
    }

    @Test
    public void shouldReturnValidationErrorWhenContactPersonIsNotEmpty() {
        Company company = SampleParty.builder()
            .withContactPerson("")
            .company();

        Set<String> validationErrors = validate(company);

        assertThat(validationErrors).hasSize(1).contains("contactPerson : may not be empty");
    }

}

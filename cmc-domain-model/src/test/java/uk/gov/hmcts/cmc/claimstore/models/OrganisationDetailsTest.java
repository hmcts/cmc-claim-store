package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class OrganisationDetailsTest {

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

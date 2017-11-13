package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class SoleTraderDetailsTest {

    @Test
    public void shouldBeValidWhenGivenNullBusinessName() {
        SoleTraderDetails soleTraderDetails = SampleTheirDetails.builder()
            .withBusinessName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(soleTraderDetails);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenGivenEmptyBusinessName() {
        SoleTraderDetails soleTraderDetails = SampleTheirDetails.builder()
            .withBusinessName(null)
            .soleTraderDetails();

        Set<String> validationErrors = validate(soleTraderDetails);

        assertThat(validationErrors).isEmpty();
    }

}

package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.party.SoleTrader;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleParty;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.BeanValidator.validate;

public class SoleTraderTest {

    @Test
    public void shouldBeValidWhenBusinessNameIsNull() {
        SoleTrader soleTrader = SampleParty.builder()
            .withBusinessName(null)
            .soleTrader();

        Set<String> validationErrors = validate(soleTrader);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenBusinessNameIsEmpty() {
        SoleTrader soleTrader = SampleParty.builder()
            .withBusinessName("")
            .soleTrader();

        Set<String> validationErrors = validate(soleTrader);

        assertThat(validationErrors).isEmpty();
    }

}

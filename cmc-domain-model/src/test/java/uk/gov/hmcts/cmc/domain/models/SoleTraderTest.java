package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.utils.BeanValidator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SoleTraderTest {

    @Test
    public void shouldBeValidWhenBusinessNameIsNull() {
        SoleTrader soleTrader = SampleParty.builder()
            .withBusinessName(null)
            .soleTrader();

        Set<String> validationErrors = BeanValidator.validate(soleTrader);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeValidWhenBusinessNameIsEmpty() {
        SoleTrader soleTrader = SampleParty.builder()
            .withBusinessName("")
            .soleTrader();

        Set<String> validationErrors = BeanValidator.validate(soleTrader);

        assertThat(validationErrors).isEmpty();
    }

}

package uk.gov.hmcts.cmc.ccd.util;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyNameUtilsTest {

    @Test
    public void shouldGetNameForIndividual() {
        Individual individual = Individual.builder().name("bla bla").build();
        assertThat(PartyNameUtils.getPartyNameFor(individual)).isEqualTo("bla bla");
    }

    @Test
    public void shouldGetNameForSoleTraderWithoutBusinessName() {
        SoleTrader soleTrader = SoleTrader.builder().name("sole trader").build();
        assertThat(PartyNameUtils.getPartyNameFor(soleTrader)).isEqualTo("sole trader");
    }

    @Test
    public void shouldGetNameForSoleTraderWithBusinessName() {
        SoleTrader soleTrader = SoleTrader.builder()
            .name("sole trader")
            .businessName("my business").build();
        assertThat(PartyNameUtils.getPartyNameFor(soleTrader))
            .isEqualTo("sole trader T/A my business");
    }
}

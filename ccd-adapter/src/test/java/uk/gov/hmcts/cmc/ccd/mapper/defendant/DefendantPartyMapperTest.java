package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.DefendantPartyAssert;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant.withPartyCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant.withPartyIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant.withPartyOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant.withPartySoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantPartyMapperTest {

    @Autowired
    private DefendantPartyMapper mapper;

    private DefendantPartyAssert assertThat(Party party) {
        return new DefendantPartyAssert(party);
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenBuilderIsNull() {
        mapper.to(null, SampleParty.builder().individual());
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenPartyIsNull() {
        mapper.to(CCDRespondent.builder(), null);
    }

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();

        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();

        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, party);

        //then
        assertThat(party).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyIndividual().build();

        //when
        Party party = mapper.from(ccdRespondent);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyCompany().build();

        //when
        Party party = mapper.from(ccdRespondent);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartySoleTrader().build();

        //when
        Party party = mapper.from(ccdRespondent);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDRespondent ccdRespondent = withPartyOrganisation().build();

        //when
        Party party = mapper.from(ccdRespondent);

        //then
        assertThat(party).isEqualTo(ccdRespondent);
    }
}

package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDPartyCompany;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDPartyIndividual;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDPartyOrganisation;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDPartySoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PartyMapperTest {

    @Autowired
    private PartyMapper partyMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartyIndividual();

        //when
        Party party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartyCompany();

        //when
        Party party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartyOrganisation();

        //when
        Party party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartySoleTrader();

        //when
        Party party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }
}

package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartyCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartyIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartyOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDPartySoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TheirDetailsMapperTest {

    @Autowired
    private TheirDetailsMapper partyMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().individualDetails();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().companyDetails();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().organisationDetails();

        //when
        CCDParty ccdParty = partyMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().soleTraderDetails();

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
        TheirDetails party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartyCompany();

        //when
        TheirDetails party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartyOrganisation();

        //when
        TheirDetails party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDParty ccdParty = getCCDPartySoleTrader();

        //when
        TheirDetails party = partyMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }
}

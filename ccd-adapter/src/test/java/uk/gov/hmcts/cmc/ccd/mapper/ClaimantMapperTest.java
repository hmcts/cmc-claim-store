package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDClaimantSoleTrader;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantMapperTest {
    @Autowired
    private ClaimantMapper claimantMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Party party = SampleParty.builder().individual();

        //when
        CCDClaimant ccdParty = claimantMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        Party party = SampleParty.builder().company();

        //when
        CCDClaimant ccdParty = claimantMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        Party party = SampleParty.builder().organisation();

        //when
        CCDClaimant ccdParty = claimantMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        Party party = SampleParty.builder().soleTrader();

        //when
        CCDClaimant ccdParty = claimantMapper.to(party);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantIndividual();

        //when
        Party party = claimantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantCompany();

        //when
        Party party = claimantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantOrganisation();

        //when
        Party party = claimantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDClaimant ccdParty = getCCDClaimantSoleTrader();

        //when
        Party party = claimantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

}

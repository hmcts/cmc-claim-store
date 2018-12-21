package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantCompany;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantIndividual;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantOrganisation;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDDefendantSoleTrader;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantMapperTest {
    @Autowired
    private DefendantMapper defendantMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().individualDetails();

        //when
        CCDDefendant ccdParty = defendantMapper.to(party, null, null);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().companyDetails();

        //when
        CCDDefendant ccdParty = defendantMapper.to(party, null, null);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().organisationDetails();

        //when
        CCDDefendant ccdParty = defendantMapper.to(party, null, null);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        TheirDetails party = SampleTheirDetails.builder().soleTraderDetails();

        //when
        CCDDefendant ccdParty = defendantMapper.to(party, null, null);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapIndividualFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantIndividual();

        //when
        TheirDetails party = defendantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapCompanyFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantCompany();

        //when
        TheirDetails party = defendantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapOrganisationFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantOrganisation();

        //when
        TheirDetails party = defendantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

    @Test
    public void shouldMapSoleTraderFromCCD() {
        //given
        CCDDefendant ccdParty = getCCDDefendantSoleTrader();

        //when
        TheirDetails party = defendantMapper.from(ccdParty);

        //then
        assertThat(party).isEqualTo(ccdParty);
    }

}

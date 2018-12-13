package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDIndividual;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IndividualMapperTest {

    @Autowired
    private IndividualMapper individualMapper;

    @Test
    public void shouldMapIndividualToCCD() {
        //given
        Individual individual = SampleParty.builder().individual();

        //when
        CCDIndividual ccdIndividual = individualMapper.to(individual);

        //then
        assertThat(individual).isEqualTo(ccdIndividual);
    }

    @Test
    public void sholdMapIndividualFromCCD() {
        //given
        CCDIndividual ccdIndividual = getCCDIndividual();

        //when
        Individual individual = individualMapper.from(ccdIndividual);

        //then
        assertThat(individual).isEqualTo(ccdIndividual);
    }

    @Test
    public void shouldNotMapIndividualDOBToCCDWhenNull() {
        //given
        Individual individual = SampleParty.builder().withDateOfBirth(null).individual();

        //when
        CCDIndividual ccdIndividual = individualMapper.to(individual);

        //then
        assertThat(individual).isEqualTo(ccdIndividual);
    }
}

package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;

import static uk.gov.hmcts.cmc.ccd.deprecated.SampleData.getCCDSoleTrader;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SoleTraderMapperTest {

    @Autowired
    private SoleTraderMapper soleTraderMapper;

    @Test
    public void shouldMapSoleTraderToCCD() {
        //given
        SoleTrader soleTrader = SampleParty.builder().soleTrader();

        //when
        CCDSoleTrader ccdSoleTrader = soleTraderMapper.to(soleTrader);

        //then
        assertThat(soleTrader).isEqualTo(ccdSoleTrader);
    }

    @Test
    public void sholdMapSoleTraderFromCCD() {
        //given
        CCDSoleTrader ccdSoleTrader = getCCDSoleTrader();

        //when
        SoleTrader soleTrader = soleTraderMapper.from(ccdSoleTrader);

        //then
        assertThat(soleTrader).isEqualTo(ccdSoleTrader);
    }
}

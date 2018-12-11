package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FullDefenceResponseMapperTest {

    @Autowired
    private FullDefenceResponseMapper fullDefenceResponseMapper;

    @Test
    public void shouldMapFullDefenceResponseToCCD() {
        //given
        final FullDefenceResponse fullDefenceResponse = SampleResponse.validDefaults();

        //when
        CCDFullDefenceResponse ccdFullDefenceResponse = fullDefenceResponseMapper.to(fullDefenceResponse);

        //then
        assertThat(fullDefenceResponse).isEqualTo(ccdFullDefenceResponse);
    }

    @Test
    public void shouldMapAlreadyPaidFullDefenceResponseToCCD() {
        //given
        FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.ALREADY_PAID)
            .withMediation(YesNoOption.NO)
            .withMoreTimeNeededOption(YesNoOption.NO)
            .build();

        //when
        CCDFullDefenceResponse ccdFullDefenceResponse = fullDefenceResponseMapper.to(fullDefenceResponse);

        //then
        assertThat(fullDefenceResponse).isEqualTo(ccdFullDefenceResponse);
    }
}

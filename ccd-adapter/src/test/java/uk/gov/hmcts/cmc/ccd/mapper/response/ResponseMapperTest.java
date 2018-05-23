package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDResponse;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ResponseMapperTest {

    @Autowired
    private ResponseMapper mapper;

    @Test
    public void shouldMapFullDefenceResponseToCCD() {
        //given
        Response response = SampleResponse.validDefaults();

        //when
        CCDResponse ccdResponse = mapper.to(response);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapFullDefenceResponseFromCCD() {
        //given
        CCDResponse ccdResponse = getCCDResponse();

        //when
        Response response = mapper.from(ccdResponse);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }
}

package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantRejectionMapperTest {

    @Autowired
    private ResponseRejectionMapper mapper;

    @Test
    public void shouldMapResponseRejectionFromCCD() {
        //given
        CCDResponseRejection ccdResponseRejection = SampleData.getResponseRejection();

        //when
        ResponseRejection response = mapper.from(ccdResponseRejection);

        //then
        assertThat(response).isEqualTo(ccdResponseRejection);
    }
}

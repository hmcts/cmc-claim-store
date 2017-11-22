package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimMapperTest {

    @Autowired
    private ClaimMapper claimMapper;


    @Test
    @Ignore
    public void shouldMapClaimToCCD() {
        //given
        ClaimData claimData = SampleClaimData.validDefaults();

        //when
        CCDClaim ccdClaim = claimMapper.to(claimData);

        //then
        // assertThat(claimData).isEqualTo(ccdClaim);
    }
}

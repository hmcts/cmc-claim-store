package uk.gov.hmcts.cmc.ccd-adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimMapperTest {

    @Autowired
    private ClaimMapper claimMapper;

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimantsFromCMCClaim() {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withClaimants(null).build())
            .build();
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        //when
        claimMapper.to(claim, builder);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingDefendantsFromCMCClaim() {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withDefendants(null).build())
            .build();
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        //when
        claimMapper.to(claim, builder);
    }
}

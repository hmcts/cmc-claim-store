package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.SampleData;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimMapperTest {

    @Autowired
    private ClaimMapper claimMapper;

    @Test
    public void shouldMapClaimToCCD() {
        //given
        ClaimData claimData = SampleClaimData.validDefaults();

        //when
        CCDClaim ccdClaim = claimMapper.to(claimData);

        //then
        assertThat(claimData).isEqualTo(ccdClaim);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimantsFromCMCClaim() {
        //given
        ClaimData claimData = SampleClaimData.builder().withClaimants(null).build();

        //when
        claimMapper.to(claimData);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingDefendantsFromCMCClaim() {
        //given
        ClaimData claimData = SampleClaimData.builder().withDefendants(null).build();

        //when
        claimMapper.to(claimData);
    }

    @Test
    public void shouldMapClaimFromCCD() {
        //given
        CCDClaim ccdClaim = SampleData.getCCDLegalClaim();

        //when
        ClaimData claimData = claimMapper.from(ccdClaim);

        //then
        assertThat(claimData).isEqualTo(ccdClaim);
    }
}

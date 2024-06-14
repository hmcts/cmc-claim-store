package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class ClaimMapperTest {

    @Autowired
    private ClaimMapper claimMapper;

    @Test
    public void shouldThrowExceptionWhenMissingClaimantsFromCMCClaim() {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withClaimants(null).build())
            .build();
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        //when
        assertThrows(NullPointerException.class, () -> {
            claimMapper.to(claim, builder);
        });
    }

    @Test
    public void shouldThrowExceptionWhenMissingDefendantsFromCMCClaim() {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withDefendants(null).build())
            .build();
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();

        //when
        assertThrows(NullPointerException.class, () -> {
            claimMapper.to(claim, builder);
        });
    }
}

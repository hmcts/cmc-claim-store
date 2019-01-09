package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantMapperTest {

    @Autowired
    private DefendantMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenTheirDetailsIsNull() {
        mapper.to(null, SampleClaim.getDefault());
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenClaimIsNull() {
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        mapper.to(theirDetails, null);
    }

    @Test
    public void mapCCJFromToCCDDefendant() {
        //Given
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        Claim claimWithCCJ = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();
        CountyCourtJudgment countyCourtJudgment = claimWithCCJ.getCountyCourtJudgment();

        //When
        CCDDefendant ccdDefendant = mapper.to(theirDetails, claimWithCCJ);

        //Then
        CCDCountyCourtJudgment ccdCountyCourtJudgment = ccdDefendant.getCountyCourtJudgement();
        assertNotNull(ccdDefendant.getCountyCourtJudgement());
        assertEquals(ccdCountyCourtJudgment.getCcjType().name(), countyCourtJudgment.getCcjType().name());
        assertEquals(ccdCountyCourtJudgment.getRequestedDate(), claimWithCCJ.getCountyCourtJudgmentRequestedAt());
    }

}

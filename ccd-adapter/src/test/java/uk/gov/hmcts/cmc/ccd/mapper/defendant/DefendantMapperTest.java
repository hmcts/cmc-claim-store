package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantMapperTest {

    @Autowired
    private DefendantMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToThrowsNullpointerWhenTheirDetailsArgIsNull() {
        mapper.to(null, SampleClaim.getDefault());
    }

    @Test(expected = NullPointerException.class)
    public void mapToThrowsNullpointerWhenClaimArgIsNull() {
        TheirDetails theirDetails = SampleTheirDetails.builder().organisationDetails();
        mapper.to(theirDetails, null);
    }

    //TODO More Tests being added as part of next PR.
}

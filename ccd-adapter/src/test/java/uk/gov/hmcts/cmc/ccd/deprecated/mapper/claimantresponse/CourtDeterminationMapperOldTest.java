package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CourtDeterminationMapperOldTest {

    @Autowired
    private CourtDeterminationMapperOld mapper;

    @Test
    public void shouldMapToCCDCourtDeterminationFromCourtDetermination() {
        //given
        CourtDetermination courtDetermination = SampleCourtDetermination.builder()
            .disposableIncome(BigDecimal.valueOf(300))
            .courtPaymentIntention(SamplePaymentIntention.instalments())
            .courtDecision(SamplePaymentIntention.instalments())
            .decisionType(DecisionType.COURT)
            .build();

        //when
        CCDCourtDetermination ccdCourtDetermination = mapper.to(courtDetermination);

        //then
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCourtDeterminationFromCCD() {
        //given
        CCDCourtDetermination ccdCourtDetermination = SampleData.getCCDCourtDetermination();

        //when
        CourtDetermination courtDetermination = mapper.from(ccdCourtDetermination);

        //then
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }
}

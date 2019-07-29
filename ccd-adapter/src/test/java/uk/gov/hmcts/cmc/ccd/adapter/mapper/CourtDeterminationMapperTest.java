package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.adapter.mapper.claimantresponse.CourtDeterminationMapper;
import uk.gov.hmcts.cmc.ccd.adapter.util.SampleData;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.math.BigDecimal;

import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CourtDeterminationMapperTest {

    @Autowired
    private CourtDeterminationMapper mapper;

    @Test
    public void shouldMapToCCDCourtDeterminationFromCourtDetermination() {
        CourtDetermination courtDetermination = SampleCourtDetermination.builder()
            .disposableIncome(BigDecimal.valueOf(300))
            .courtPaymentIntention(SamplePaymentIntention.instalments())
            .courtDecision(SamplePaymentIntention.instalments())
            .decisionType(DecisionType.COURT)
            .build();
        CCDCourtDetermination ccdCourtDetermination = mapper.to(courtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCCDCourtDeterminationFromCourtDeterminationWithCourtPaymentIntentionPayBySetDate() {
        CourtDetermination courtDetermination = SampleCourtDetermination.builder()
            .disposableIncome(BigDecimal.valueOf(300))
            .courtPaymentIntention(SamplePaymentIntention.bySetDate())
            .courtDecision(SamplePaymentIntention.instalments())
            .decisionType(DecisionType.COURT)
            .build();
        CCDCourtDetermination ccdCourtDetermination = mapper.to(courtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCCDCourtDeterminationFromCourtDeterminationWithCourtPaymentIntentionPayImmediately() {
        CourtDetermination courtDetermination = SampleCourtDetermination.builder()
            .disposableIncome(BigDecimal.valueOf(300))
            .courtPaymentIntention(SamplePaymentIntention.immediately())
            .courtDecision(SamplePaymentIntention.instalments())
            .decisionType(DecisionType.COURT)
            .build();
        CCDCourtDetermination ccdCourtDetermination = mapper.to(courtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCCDCourtDeterminationWithPayByInstalmentsToCourtDetermination() {
        CCDCourtDetermination ccdCourtDetermination = SampleData.getCCDCourtDetermination();
        CourtDetermination courtDetermination = mapper.from(ccdCourtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCCDCourtDeterminationWithPayImmediatelyToCourtDetermination() {
        CCDCourtDetermination ccdCourtDetermination = SampleData.getCCDCourtDeterminationImmediately();
        CourtDetermination courtDetermination = mapper.from(ccdCourtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }

    @Test
    public void shouldMapToCCDCourtDeterminationWithPayBySetDateToCourtDetermination() {
        CCDCourtDetermination ccdCourtDetermination = SampleData.getCCDCourtDeterminationPayBySetDate();
        CourtDetermination courtDetermination = mapper.from(ccdCourtDetermination);
        assertThat(courtDetermination).isEqualTo(ccdCourtDetermination);
    }
}

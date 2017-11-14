package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmccase.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ContentProviderTest {

    private Claim claim = SampleClaim.builder()
        .withClaimData(SampleClaimData.validDefaults())
        .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately().build())
        .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    private ContentProvider provider;

    @Before
    public void setup() {
        this.provider = new ContentProvider(new InterestCalculationService(Clock.systemDefaultZone()));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaim() {
        provider.createContent(null);
    }


    @Test
    public void shouldPopulateMap() {
        Map<String, Object> contentMap = provider.createContent(claim);

        assertThat(contentMap).isNotNull();
        assertThat(contentMap.get("ccj")).isNotNull();
    }

}

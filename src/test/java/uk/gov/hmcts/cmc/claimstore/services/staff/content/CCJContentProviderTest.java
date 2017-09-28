package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CCJContentProviderTest {

    private Claim claim = SampleClaim.builder()
        .withClaimData(SampleClaimData.validDefaults())
        .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately().build())
        .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    private CCJContentProvider provider;

    @Before
    public void setup() {
        this.provider = new CCJContentProvider();
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
        assertThat(((CCJContent) contentMap.get("ccj")).getDefendantName()).isNotEmpty();
    }

}

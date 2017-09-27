package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class CCJContentProviderTest {

    private Claim claim = SampleClaim.builder()
        .withClaimData(SampleClaimData.validDefaults())
        .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately().build())
        .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    @Mock
    private InterestCalculationService interestCalculationService;

    private CCJContentProvider provider;

    @Before
    public void setup() {
        this.provider = new CCJContentProvider(new InterestContentProvider(interestCalculationService));

        Mockito.when(interestCalculationService.calculateInterestUpToNow(
            any(), any(), any())
        ).thenReturn(BigDecimal.TEN);
        Mockito.when(interestCalculationService.calculateDailyAmountFor(
            any(), any())
        ).thenReturn(BigDecimal.TEN);

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
        assertThat(((CCJContent) contentMap.get("ccj")).getClaimantName()).isNotEmpty();
    }

}

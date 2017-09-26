package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class CCJContentProviderTest {

    private Claim claim = SampleClaim.builder().build();
    @Mock
    private InterestCalculationService interestCalculationService;

    private CCJContentProvider provider;

    @Before
    public void setup() {
        this.provider = new CCJContentProvider(
            new InterestContentProvider(interestCalculationService)
        );

    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaim() {
        provider.createContent(null);
    }


    @Test
    public void shouldProvideAFullName() {
        Map<String, Object> contentMap = provider.createContent(claim);

        assertThat(contentMap).isNotNull();
        assertThat(contentMap.get("ccj")).isNotNull();
        assertThat(((CCJContent) contentMap.get("ccj")).getClaimantName()).isNotEmpty();
    }

    @Test
    public void shouldProvideAnAddress() {
        Map<String, Object> contentMap = provider.createContent(claim);

        assertThat(contentMap).isNotNull();
        assertThat(contentMap.get("ccj")).isNotNull();
        assertThat(((CCJContent) contentMap.get("ccj")).getClaimantName()).isNotNull();
    }
}

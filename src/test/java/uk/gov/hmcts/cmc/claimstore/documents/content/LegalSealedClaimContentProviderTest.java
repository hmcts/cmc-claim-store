package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.math.BigInteger;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegalSealedClaimContentProviderTest {
    @Mock
    private StatementOfValueProvider statementOfValueProvider;

    @Test
    public void shouldCreateContent() throws Exception {
        //given
        final Claim claim = SampleClaim.builder().withClaimData(
            SampleClaimData.builder().withFeeAmount(BigInteger.valueOf(50001)).build()
        ).build();

        final LegalSealedClaimContentProvider legalSealedClaimContentProvider
            = new LegalSealedClaimContentProvider(statementOfValueProvider);

        //when
        final Map<String, Object> contents = legalSealedClaimContentProvider.createContent(claim);

        //then
        assertThat(contents).isNotEmpty().containsKey("feePaid").containsValue("£500.01");
    }

}

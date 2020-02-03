package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.math.BigInteger;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LegalSealedClaimDataContentProviderTest {
    @Mock
    private StatementOfValueProvider statementOfValueProvider;

    @Test
    public void shouldCreateContent() {
        //given
        Claim claim = SampleClaim.builder().withClaimData(
            SampleClaimData.builder().withFeeAmount(BigInteger.valueOf(50001)).build()
        ).build();

        LegalSealedClaimContentProvider legalSealedClaimContentProvider
            = new LegalSealedClaimContentProvider(statementOfValueProvider, false);

        //when
        Map<String, Object> contents = legalSealedClaimContentProvider.createContent(claim);

        //then
        assertThat(contents).isNotEmpty().containsKey("feePaid").containsValue("£500.01");
    }

    @Test
    public void contentShouldIncludeWaterMarkFlag() {
        //given
        Claim claim = SampleClaim.getDefaultForLegal();

        LegalSealedClaimContentProvider legalSealedClaimContentProvider
            = new LegalSealedClaimContentProvider(statementOfValueProvider, true);

        //when
        Map<String, Object> contents = legalSealedClaimContentProvider.createContent(claim);

        //then
        assertThat(contents).isNotEmpty().containsKey("watermarkPdf");
    }
}

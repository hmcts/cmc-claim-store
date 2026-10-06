package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.math.BigInteger;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void shouldThrowExceptionWhenMissingIssuedDate() {
        assertThrows(IllegalStateException.class, () -> {
            Claim claim = SampleClaim.getDefaultForLegal().toBuilder().issuedOn(null).build();

            LegalSealedClaimContentProvider legalSealedClaimContentProvider
                = new LegalSealedClaimContentProvider(statementOfValueProvider, false);

            legalSealedClaimContentProvider.createContent(claim);
        });
    }
}

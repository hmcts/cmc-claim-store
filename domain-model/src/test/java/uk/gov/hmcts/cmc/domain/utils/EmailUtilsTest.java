package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailUtilsTest {

    private static final String DEFENDANT_EMAIL = "ineed@coffee.test";

    @Test
    public void shouldGetTheDefendantEmailIfPopulated() {
        Claim claim = SampleClaim.builder().withDefendantEmail(DEFENDANT_EMAIL).build();
        Optional<String> defendantEmail = EmailUtils.getDefendantEmail(claim);
        assertThat(defendantEmail).contains(DEFENDANT_EMAIL);
    }

    @Test
    public void shouldGetTheDefendantEmailIfPopulatedInClaimData() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendant(
                        SampleTheirDetails.builder()
                            .withEmail(DEFENDANT_EMAIL)
                            .individualDetails()
                    )
                    .build()
            ).build();
        Optional<String> defendantEmail = EmailUtils.getDefendantEmail(claim);
        assertThat(defendantEmail).contains(DEFENDANT_EMAIL);
    }

    @Test
    public void shouldReturnEmptyIfNotPopulated() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(null)
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendant(
                        SampleTheirDetails.builder()
                            .withEmail(null)
                            .individualDetails()
                    )
                    .build()
            ).build();
        Optional<String> defendantEmail = EmailUtils.getDefendantEmail(claim);
        assertThat(defendantEmail).isNotPresent();
    }
}

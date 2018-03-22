package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.isSealedClaim;

public class DocumentNameUtilsTest {

    @Test(expected = NullPointerException.class)
    public void shouldThrowErrorWhenNumberIsNullWhileBuildingSealedClaimFileBaseName() {
        buildSealedClaimFileBaseName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorWhenNumberIsEmptyWhileBuildingSealedClaimFileBaseName() {
        buildSealedClaimFileBaseName("");
    }

    @Test
    public void shouldBuildSealedClaimFileBaseName() {
        assertThat(buildSealedClaimFileBaseName("000MC001"))
            .isEqualTo("000MC001-claim-form");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowErrorWhenNumberIsNullWhileBuildingDefendantLetterFileBaseName() {
        buildDefendantLetterFileBaseName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorWhenNumberIsEmptyWhileBuildingDefendantLetterFileBaseName() {
        buildDefendantLetterFileBaseName("");
    }

    @Test
    public void shouldBuildDefendantLetterFileBaseName() {
        assertThat(buildDefendantLetterFileBaseName("000MC001"))
            .isEqualTo("000MC001-defendant-pin-letter");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowErrorWhenFilenameIsNullWhileCheckingWhetherFilenameIndicatesSealedClaim() {
        isSealedClaim(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorWhenFilenameIsEmptyWhileCheckingWhetherFilenameIndicatesSealedClaim() {
        isSealedClaim("");
    }

    @Test
    public void shouldReturnTrueIfFilenameIndicatesSealedClaim() {
        assertThat(isSealedClaim(buildSealedClaimFileBaseName("000MC001")))
            .isTrue();
    }

    @Test
    public void shouldReturnFalseIfFilenameDoesNotIndicateSealedClaim() {
        assertThat(isSealedClaim(buildDefendantLetterFileBaseName("000MC001")))
            .isFalse();
    }

}

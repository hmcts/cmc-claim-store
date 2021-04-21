package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildBreathingSpaceEnteredFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildBreathingSpaceLiftedFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDraftClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJudgeDirectionOrderFileName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLADirectionOrderFileName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildNoticeOfTransferToCcbcForDefendantFileName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOcon9FormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForInterlocutoryJudgmentFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgmentByAdmissionOrDeterminationFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForReferToJudgeFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestOrgRepaymentFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildReviewOrderFileBaseName;
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

    @Test
    public void shouldBuildDraftClaimFileBaseName() {
        assertThat(buildDraftClaimFileBaseName("externalId"))
            .isEqualTo("draft-claim-externalId");
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

    @Test(expected = NullPointerException.class)
    public void shouldThrowErrorWhenReferenceIsNullWhileBuildingReviewOrderFileBaseName() {
        buildReviewOrderFileBaseName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorWhenReferenceIsEmptyWhileBuildingReviewOrderFileBaseName() {
        buildReviewOrderFileBaseName("");
    }

    @Test
    public void shouldBuildReviewOrderFileBaseName() {
        assertThat(buildReviewOrderFileBaseName("000MC001"))
            .isEqualTo("000MC001-review-order");
    }

    @Test
    public void shouldBuildLetterFileBaseName() {
        String date = LocalDate.now().toString();
        assertThat(buildLetterFileBaseName("000MC001", date))
            .isEqualTo("000MC001-general-letter-" + date);
    }

    @Test
    public void shouldBuildJudgmentByAdmissionOrDeterminationFileBaseName() {
        assertThat(buildRequestForJudgmentByAdmissionOrDeterminationFileBaseName("000MC001", "admissions"))
            .isEqualTo("000MC001-ccj-request-admissions");
    }

    @Test
    public void shouldBuildInterlocutoryJudgmentFileBaseName() {
        assertThat(buildRequestForInterlocutoryJudgmentFileBaseName("000MC001"))
            .isEqualTo("000MC001-request-interloc-judgment");
    }

    @Test
    public void shouldBuildReferToJugdeBaseName() {
        assertThat(buildRequestForReferToJudgeFileBaseName("000MC001", "claimant"))
            .isEqualTo("000MC001-request-redeterm-claimant");
    }

    @Test
    public void shouldBuildClaimantResponseFileBaseName() {
        assertThat(buildClaimantResponseFileBaseName("000MC001"))
            .isEqualTo("000MC001-claimant-response");
    }

    @Test
    public void shouldBuildNoticeOfTransferToCcbcForDefendantFileName() {
        assertThat(buildNoticeOfTransferToCcbcForDefendantFileName("000MC001"))
            .isEqualTo("000MC001-defendant-case-handoff");
    }

    @Test
    public void shouldBuildRequestOrgRepaymentFileBaseName() {
        assertThat(buildRequestOrgRepaymentFileBaseName("000MC001"))
            .isEqualTo("000MC001-request-org-repayment-amount");
    }

    @Test
    public void shouldBuildJudgeDirectionOrderFileName() {
        assertThat(buildJudgeDirectionOrderFileName("000MC001")).isEqualTo("000MC001-Judge-Directions-Order");
    }

    @Test
    public void shouldBuildLADirectionOrderFileName() {
        assertThat(buildLADirectionOrderFileName("000MC001"))
            .isEqualTo("000MC001-Legal-Adviser-Directions-Order");
    }

    @Test
    public void shouldBuildOcon9FormFileBaseName() {
        assertThat(buildOcon9FormFileBaseName("000MC001"))
            .isEqualTo("000MC001-issue-OCON9-form");

    @Test
    public void shouldBuildBreathingSpaceEnteredFileBaseNameFromCCD() {
        assertThat(buildBreathingSpaceEnteredFileBaseName("000MC001", true))
            .isEqualTo("000MC001-breathing-space-entered.pdf");
    }

    @Test
    public void shouldBuildBreathingSpaceEnteredFileBaseName() {
        assertThat(buildBreathingSpaceEnteredFileBaseName("000MC001", false))
            .isEqualTo("000MC001-breathing-space-entered");
    }

    @Test
    public void shouldBuildBreathingSpaceLiftedFileBaseNameFromCCD() {
        assertThat(buildBreathingSpaceLiftedFileBaseName("000MC001", true))
            .isEqualTo("000MC001-breathing-space-lifted.pdf");
    }

    @Test
    public void shouldBuildBreathingSpaceLiftedFileBaseName() {
        assertThat(buildBreathingSpaceLiftedFileBaseName("000MC001", false))
            .isEqualTo("000MC001-breathing-space-lifted");
    }

}

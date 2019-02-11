package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isReferredToJudge;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isSettlePreJudgment;

public class ClaimantResponseHelperTest {

    @Test
    public void shouldReturnTrueWhenSettlePreJudgment() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceSettlePreJudgement();
        assertThat(isSettlePreJudgment(claimantResponse)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenRejectSettlePreJudgment() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseRejection.builder()
            .buildRejectionSettlePreJudgement();
        assertThat(isSettlePreJudgment(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenSettlePreJudgmentNotPresent() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();
        assertThat(isSettlePreJudgment(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenIsReferredToJudge() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptationReferToJudgeWithCourtDetermination();
        assertThat(isReferredToJudge(claimantResponse)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenIsNotReferredToJudge() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceIssueSettlementWithClaimantPaymentIntentionPayImmediately();
        assertThat(isReferredToJudge(claimantResponse)).isFalse();
    }

}

package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isIntentToProceed;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isReferredToJudge;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isSettlePreJudgment;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleHearingLocation.pilotHearingLocation;

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

    @Test
    public void shouldReturnTrueWhenOptedForMediation() {
        SampleClaimantResponse.ClaimantResponseRejection.builder();
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .amountPaid(TEN)
            .freeMediation(YES)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Some valid reason")
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder()
                .withHearingLocation(pilotHearingLocation).build())
            .build();

        assertThat(isOptedForMediation(claimantResponse)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenNotOptedForMediation() {
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .amountPaid(TEN)
            .freeMediation(NO)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Some valid reason")
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .build();

        assertThat(isOptedForMediation(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenFreeMediationIsNull() {
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .amountPaid(TEN)
            .freeMediation(null)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Some valid reason")
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .build();

        assertThat(isOptedForMediation(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnFalseForIsIntentToProceedWhenNoDirectionsQuestionnaire() {
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .amountPaid(TEN)
            .freeMediation(NO)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Some valid reason")
            .build();

        assertThat(isIntentToProceed(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnFalseForIsIntentToProceedWhenResponseAcceptation() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        assertThat(isIntentToProceed(claimantResponse)).isFalse();
    }

    @Test
    public void shouldReturnTrueForIsIntentToProceedWhenHasDirectionsQuestionnaire() {
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .amountPaid(TEN)
            .freeMediation(NO)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Some valid reason")
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .build();

        assertThat(isIntentToProceed(claimantResponse)).isTrue();
    }
}

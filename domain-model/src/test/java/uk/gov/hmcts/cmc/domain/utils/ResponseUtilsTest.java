package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ResponseUtilsTest {

    private static final String MISSING_PAYMENT_DECLARATION_DATE = "Missing payment declaration date";

    @Test
    public void isResponseStatesPaidOnFullDefenceAlreadyPaidResponseShouldBeTrue() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isTrue();
    }

    @Test
    public void isResponseStatesPaidOnPartAdmissionWithPaymentDeclarationShouldBeTrue() {
        Response response = PartAdmissionResponse.builder()
            .paymentDeclaration(SamplePaymentDeclaration.builder().build()).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isTrue();
    }

    @Test
    public void isResponseStatesPaidOnNullResponseShouldBeFalse() {
        assertThat(ResponseUtils.isResponseStatesPaid(null)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnFullAdmissionShouldBeFalse() {
        Response response = SampleResponse.FullAdmission.builder().build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnFullDefenseWithDisputeShouldBeFalse() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseStatesPaidOnPartAdmissionWithNoPaymentDeclarationShouldBeFalse() {
        Response response = PartAdmissionResponse.builder().paymentDeclaration(null).build();

        assertThat(ResponseUtils.isResponseStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseFullDefenceStatesPaidShouldBeTrue() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build();

        assertThat(ResponseUtils.isResponseFullDefenceStatesPaid(response)).isTrue();
    }

    @Test
    public void isResponseFullDefenceStatesPaidFullDefenseWithDisputeShouldBeFalse() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE).build();

        assertThat(ResponseUtils.isResponseFullDefenceStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseStatesPaidAcceptedShouldBeTrue() {
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();

        assertThat(ResponseUtils.isResponseStatesPaidAccepted(claim)).isTrue();
    }

    @Test
    public void isResponseStatesPaidAcceptedShouldBeFalse() {
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithRejection();

        assertThat(ResponseUtils.isResponseStatesPaidAccepted(claim)).isFalse();
    }

    @Test
    public void isResponseStatesPaidAcceptedShouldBeFalseWhenNoResponse() {
        Claim claim = SampleClaim.getCitizenClaim();

        assertThat(ResponseUtils.isResponseStatesPaidAccepted(claim)).isFalse();
    }

    @Test
    public void isResponseStatesPaidAcceptedShouldBeFalseWhenNoClaimantResponse() {
        Claim claim = SampleClaim.getClaimWithFullDefenceAlreadyPaid();

        assertThat(ResponseUtils.isResponseStatesPaidAccepted(claim)).isFalse();
    }

    @Test
    public void shouldReturnPaymentDeclarationDateStatesPaidFullDefence() {
        Response response = SampleResponse.FullDefence.validDefaults();

        assertThat(ResponseUtils.statesPaidPaymentDeclarationDate(response))
            .isEqualTo((LocalDate.of(2016, 1, 2).toString()));
    }

    @Test
    public void shouldReturnPaymentDeclarationDateStatesPaidPartAdmission() {
        Response response = SampleResponse.PartAdmission.builder()
            .buildWithStatesPaid(SampleParty.builder().individual());

        assertThat(ResponseUtils.statesPaidPaymentDeclarationDate(response))
            .isEqualTo((LocalDate.of(2016, 1, 2).toString()));
    }

    @Test
    public void shouldReturnExceptionWhenInvalidResponseType() {
        Response response = SampleResponse.FullAdmission.builder().build();
        Exception exception = assertThrows(IllegalStateException.class,
            () -> ResponseUtils.statesPaidPaymentDeclarationDate(response)
        );

        assertTrue(exception.getMessage().contains("Invalid response type "));
    }

    @Test
    public void shouldReturnExceptionWhenNotFullDefenceStatesPaid() {
        Response response = SampleResponse.FullDefence.builder()
            .withDefenceType(DefenceType.DISPUTE)
            .withPaymentDeclaration(null)
            .build();
        Exception exception = assertThrows(IllegalStateException.class,
            () -> ResponseUtils.statesPaidPaymentDeclarationDate(response)
        );

        assertTrue(exception.getMessage().contains(MISSING_PAYMENT_DECLARATION_DATE));
    }

    @Test
    public void shouldReturnExceptionWhenNotPartAdmissionStatesPaid() {
        Response response = SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately();
        Exception exception = assertThrows(IllegalStateException.class,
            () -> ResponseUtils.statesPaidPaymentDeclarationDate(response)
        );

        assertTrue(exception.getMessage().contains(MISSING_PAYMENT_DECLARATION_DATE));
    }

    @Test
    public void isResponseFullDefenceStatesPaidFullAdmissionShouldBeFalse() {
        Response response = SampleResponse.FullAdmission.builder().build();

        assertThat(ResponseUtils.isResponseFullDefenceStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseFullDefenceStatesPaidPartAdmissionWithPaymentDeclarationShouldBeFalse() {
        Response response = PartAdmissionResponse.builder()
            .paymentDeclaration(SamplePaymentDeclaration.builder().build()).build();

        assertThat(ResponseUtils.isResponseFullDefenceStatesPaid(response)).isFalse();
    }

    @Test
    public void isResponseFullDefenceStatesPaidNullResponseShouldBeFalse() {
        assertThat(ResponseUtils.isResponseFullDefenceStatesPaid(null)).isFalse();
    }

    @Test
    public void isResponsePartAdmitPayImmediatelyOnPartAdmissionWithPayImmediatelyShouldBeTrue() {
        Response response = PartAdmissionResponse.builder().paymentIntention(
            PaymentIntention.builder().paymentOption(PaymentOption.IMMEDIATELY).build()
        ).build();

        assertThat(ResponseUtils.isResponsePartAdmitPayImmediately(response)).isTrue();
    }

    @Test
    public void isResponsePartAdmitPayImmediatelyOnPartAdmissionWithNoPaymentOptionShouldBeFalse() {
        Response response = PartAdmissionResponse.builder().paymentIntention(
            PaymentIntention.builder().paymentOption(null).build()
        ).build();

        assertThat(ResponseUtils.isResponsePartAdmitPayImmediately(response)).isFalse();
    }

    @Test
    public void isResponsePartAdmitPayImmediatelyOnPartAdmissionWithNoPaymentIntentionShouldBeFalse() {
        Response response = PartAdmissionResponse.builder().paymentIntention(null).build();

        assertThat(ResponseUtils.isResponsePartAdmitPayImmediately(response)).isFalse();
    }

    @Test
    public void isResponsePartAdmitPayImmediatelyOnNonPartAdmissionShouldBeFalse() {
        Response response = FullAdmissionResponse.builder().build();

        assertThat(ResponseUtils.isResponsePartAdmitPayImmediately(response)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenFullAdmissionResponse() {
        Response response = FullAdmissionResponse.builder().build();
        assertThat(ResponseUtils.isAdmissionResponse(response)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenPartAdmissionResponse() {
        Response response = PartAdmissionResponse.builder().build();
        assertThat(ResponseUtils.isAdmissionResponse(response)).isTrue();
        assertThat(ResponseUtils.isPartAdmission(response)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenNonAdmissionResponse() {
        Response response = FullDefenceResponse.builder().build();
        assertThat(ResponseUtils.isAdmissionResponse(response)).isFalse();
        assertThat(ResponseUtils.isPartAdmission(response)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenFullAdmitResponse() {
        Response response = FullAdmissionResponse.builder().build();
        assertThat(ResponseUtils.isPartAdmission(response)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenFullDefenceAndNoMediation() {
        Response response = SampleResponse.FullDefence.builder().withMediation(YesNoOption.NO).build();

        assertThat(ResponseUtils.isFullDefenceAndNoMediation(response)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenFullDefenceAndYesMediation() {
        Response response = SampleResponse.FullDefence.builder().withMediation(YesNoOption.YES).build();

        assertThat(ResponseUtils.isFullDefenceAndNoMediation(response)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenFullDefenceDisputeAndNoMediation() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE)
            .withMediation(YesNoOption.NO).build();

        assertThat(ResponseUtils.isFullDefenceDisputeAndNoMediation(response)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenFullDefenceDisputeOptionAndNoMediation() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE)
            .withMediation(YesNoOption.NO).build();

        assertThat(ResponseUtils.isFullDefenceDispute(response)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenFullDefenceDisputeOptionAndYesMediation() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.DISPUTE)
            .withMediation(YesNoOption.YES).build();

        assertThat(ResponseUtils.isFullDefenceDispute(response)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenNotDisputeOption() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build();

        assertThat(ResponseUtils.isFullDefenceDispute(response)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenNotFullDefense() {
        Response response = SampleResponse.PartAdmission.builder().build();

        assertThat(ResponseUtils.isFullDefenceDispute(response)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenFullDefenceAlreadyPaidAndYesMediation() {
        Response response = SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID)
            .withMediation(YesNoOption.YES).build();

        assertThat(ResponseUtils.isFullDefenceDisputeAndNoMediation(response)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenDefendantHasNotOptedForMediation() {
        Response response = SampleResponse.FullDefence.builder().withMediation(YesNoOption.NO).build();

        assertThat(ResponseUtils.hasDefendantOptedForMediation(response)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenDefendantHaOptedForMediation() {
        Response response = SampleResponse.FullDefence.builder().withMediation(YesNoOption.YES).build();

        assertThat(ResponseUtils.hasDefendantOptedForMediation(response)).isTrue();
    }
}



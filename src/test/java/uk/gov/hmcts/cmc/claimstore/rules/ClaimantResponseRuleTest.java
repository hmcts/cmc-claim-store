package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseRuleTest {

    private final ClaimantResponseRule claimantResponseRule = new ClaimantResponseRule();

    @Test
    public void shouldNotThrowExceptionWhenClaimantResponseCanBeRequested() {
        Claim claim = SampleClaim.builder().withRespondedAt(now().minusDays(2)).build();
        assertThatCode(() ->
            claimantResponseRule.assertCanBeRequested(claim, USER_ID)
        ).doesNotThrowAnyException();
    }

    @Test(expected = ClaimantLinkException.class)
    public void shouldThrowExceptionWheClaimantIsNotLinkedToTheCase() {
        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(1));
        claimantResponseRule.assertCanBeRequested(claim, "2");
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenClaimWasNotRespondedTo() {
        Claim claim = SampleClaim.builder().build();
        claimantResponseRule.assertCanBeRequested(claim, USER_ID);
    }

    @Test(expected = ClaimantResponseAlreadySubmittedException.class)
    public void shouldThrowExceptionWhenClaimantResponseWasAlreadySubmitted() {
        Claim claim = SampleClaim.builder().withRespondedAt(now().minusDays(2))
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withClaimantRespondedAt(now())
            .build();
        claimantResponseRule.assertCanBeRequested(claim, USER_ID);
    }

    @Test()
    public void shouldBeValidWhenClaimantIsBusiness() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().withDefendant(
                    SampleTheirDetails.builder().companyDetails()
                ).build()
            )
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withClaimantRespondedAt(now())
            .build();
        assertThatCode(() ->
            claimantResponseRule.isValid(claim)
        ).doesNotThrowAnyException();
    }

    @Test()
    public void shouldBeValidWhenNoFormaliseOptionExpected() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().build())
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withClaimantRespondedAt(now())
            .build();
        assertThatCode(() ->
            claimantResponseRule.isValid(claim)
        ).doesNotThrowAnyException();
    }

    @Test()
    public void shouldBeValidWhenFormaliseOptionExpected() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().build()
            )
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withClaimantRespondedAt(now())
            .build();
        assertThatCode(() ->
            claimantResponseRule.isValid(claim)
        ).doesNotThrowAnyException();
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenFormaliseOptionExpectedButCourtDeterminationMissing() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueSettlementWithClaimantPaymentIntention();
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().build()
            )
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withClaimantResponse(claimantResponse)
            .withClaimantRespondedAt(now())
            .build();
        claimantResponseRule.isValid(claim);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenFormaliseOptionExpectedMissing() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .withFormaliseOption(null)
            .build();
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().build()
            )
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withClaimantResponse(claimantResponse)
            .withClaimantRespondedAt(now())
            .build();
        claimantResponseRule.isValid(claim);
    }

    @Test
    public void shouldBeValidWhenFormaliseOptionExpectedAndValidClaimantResponseState() {
        ClaimantResponse claimantResponse = SampleClaimantResponse
            .ClaimantResponseAcceptation
            .builder()
            .buildAcceptationIssueSettlementWithCourtDetermination();
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().build()
            )
            .withRespondedAt(now().minusDays(2))
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withClaimantResponse(claimantResponse)
            .withClaimantRespondedAt(now())
            .build();
        assertThatCode(() ->
            claimantResponseRule.isValid(claim)
        ).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotHaveFormaliseOptionWhenPartAdmissionAndStatesPaidResponse() {
        Response response = SampleResponse
            .PartAdmission
            .builder()
            .buildWithStatesPaid(SampleParty.builder().individual());
        assertThat(ClaimantResponseRule.isFormaliseOptionExpectedForResponse(response)).isFalse();
    }
}

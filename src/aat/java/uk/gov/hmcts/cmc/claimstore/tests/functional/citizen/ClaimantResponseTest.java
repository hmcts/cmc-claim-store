package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.SETTLEMENT;

public class ClaimantResponseTest extends BaseTest {
    private static final BigDecimal TEN_2DP = TEN.setScale(2, RoundingMode.UNNECESSARY);

    private User claimant;
    private Claim claim;

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();

        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        claim = createClaimWithResponse(createdCase, defendant);
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @LogExecutionTime
    @Retry
    public void shouldSaveClaimantResponseAcceptationReferToJudge() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(() -> new AssertionError(MISSING_CLAIMANT_RESPONSE));

        assertThat(claimantResponse.getAmountPaid()).contains(TEN_2DP);
    }

    @Test
    @Retry
    public void shouldSaveClaimantResponseAcceptationIssueCCJWithDefendantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithDefendantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsCCJ(claimWithClaimantResponse);
    }

    private void assertClaimantResponseFormaliseAsCCJ(Claim claimWithClaimantResponse) {
        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(() -> new AssertionError(MISSING_CLAIMANT_RESPONSE));

        assertThat(claimantResponse.getAmountPaid()).contains(TEN_2DP);
        assertThat(claimantResponse.getFormaliseOption()).contains(CCJ);
        CountyCourtJudgment countyCourtJudgment = claimWithClaimantResponse.getCountyCourtJudgment();
        assertThat(countyCourtJudgment).isNotNull();
        assertThat(countyCourtJudgment.getPaymentOption()).isEqualTo(PaymentOption.BY_SPECIFIED_DATE);
    }

    @Test
    @Retry
    public void shouldNotSaveClaimantResponseAcceptationIssueCCJWithClaimantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithClaimantPaymentIntentionBySetDate(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Retry
    public void shouldSaveClaimantResponseAcceptationIssueCCJWithCourtDetermination() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithCourtDetermination(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsCCJ(claimWithClaimantResponse);
    }

    @Test
    @Retry
    public void shouldSaveClaimantResponseAcceptationIssueSettlementWithCourtDetermination() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithCourtDetermination(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsSettlement(claimWithClaimantResponse);
    }

    @Test
    @Retry
    public void shouldSaveClaimantResponseAcceptationIssueSettlementWithDefendantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithDefendantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsSettlement(claimWithClaimantResponse);
    }

    private void assertClaimantResponseFormaliseAsSettlement(Claim claimWithClaimantResponse) {
        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(() -> new AssertionError(MISSING_CLAIMANT_RESPONSE));

        assertThat(claimantResponse.getAmountPaid()).contains(TEN_2DP);
        assertThat(claimantResponse.getFormaliseOption()).contains(SETTLEMENT);
        assertThat(claimWithClaimantResponse.getCountyCourtJudgment()).isNull();
        assertThat(claimWithClaimantResponse.getSettlement()).isNotEmpty();
    }

    @Test
    @Retry
    public void shouldSaveClaimantResponseRejection() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();

        ResponseRejection claimantResponse = (ResponseRejection) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(() -> new AssertionError(MISSING_CLAIMANT_RESPONSE));

        assertThat(claimantResponse.getFreeMediation()).isNotEmpty();
        assertThat(claimantResponse.getAmountPaid()).contains(TEN_2DP);
    }

    private Claim createClaimWithResponse(Claim createdCase, User defendant) {
        commonOperations.linkDefendant(
            defendant.getAuthorisation(), createdCase.getLetterHolderId()
        );

        Response response = SampleResponse.PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }
}

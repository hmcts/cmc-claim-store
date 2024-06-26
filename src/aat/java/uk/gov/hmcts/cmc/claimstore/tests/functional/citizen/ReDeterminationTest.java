package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ReDeterminationTest extends BaseTest {

    private User claimant;
    private Claim claim;

    @BeforeEach
    public void before() {
        claimant = bootstrap.getClaimant();

        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        claim = createClaimWithResponse(createdCase, defendant);
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    public void shouldSaveReDeterminationWithCourtDetermination() {
        String explanation = "I want it sooner";
        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithCourtDetermination(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.OK.value());

        Claim claimWithReDetermination
            = commonOperations.retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithReDetermination.getReDeterminationRequestedAt()).isNotEmpty();
        ReDetermination reDetermination = claimWithReDetermination.getReDetermination()
            .orElseThrow(() -> new AssertionError("Missing redetermination"));

        assertThat(reDetermination.getExplanation()).isEqualTo(explanation);
    }

    @Test
    @Retry
    public void shouldNotSaveReDeterminationByClaimantPaymentIntentionWithoutCourtDetermination() {
        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithClaimantPaymentIntentionBySetDate(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Retry
    public void shouldSaveReDeterminationWithDefendantPaymentIntentionAccepted() {
        String explanation = "I want it sooner";
        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithDefendantPaymentIntention(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.OK.value());

        Claim claimWithReDetermination
            = commonOperations.retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithReDetermination.getReDeterminationRequestedAt()).isNotEmpty();
        ReDetermination reDetermination = claimWithReDetermination.getReDetermination()
            .orElseThrow(() -> new AssertionError("Missing redetermination"));

        assertThat(reDetermination.getExplanation()).isEqualTo(explanation);
    }

    @Test
    @Retry
    public void shouldReturnUnprocessableEntityWhenInvalidReDeterminationIsSubmitted() {
        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(null).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    @Retry
    public void shouldNotBeAllowedToRequestReDeterminationWhenNoClaimantResponseGiven() {
        String explanation = "I want it sooner";

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("message", containsString("County Court Judgment for the claim "
                + claim.getExternalId()
                + " is not yet submitted"));
    }

    @Test
    @Retry
    public void shouldNotBeAllowedToRequestReDeterminationWhenAlreadyReDetermined() {
        String explanation = "I want it sooner";

        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithDefendantPaymentIntention(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.OK.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("message", containsString("County Court Judgment for the claim "
                + claim.getExternalId()
                + " has been already redetermined"));
    }

    @Test
    @Retry
    public void shouldNotBeAllowedToRequestReDeterminationWhenNoCCJRequested() {
        String explanation = "I want it sooner";

        commonOperations.submitClaimantResponse(
                ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithCourtDetermination(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
                ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
                claim.getExternalId(),
                claimant
            ).then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .body("message", containsString("County Court Judgment for the claim "
                + claim.getExternalId()
                + " is not yet submitted"));
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

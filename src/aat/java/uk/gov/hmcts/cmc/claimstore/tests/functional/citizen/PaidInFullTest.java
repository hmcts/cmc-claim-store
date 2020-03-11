package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;

import java.util.UUID;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

public class PaidInFullTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();
    }

    @Test
    public void shouldSuccessfullySavePaidInFull() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        PaidInFull paidInFull = new PaidInFull(now());

        Claim updatedCase = commonOperations
            .paidInFull(createdCase.getExternalId(), paidInFull, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getMoneyReceivedOn())
            .contains(paidInFull.getMoneyReceivedOn());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidRequestBodyIsSubmitted() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        commonOperations
            .paidInFull(createdCase.getExternalId(), new PaidInFull(null), claimant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenPaidInFullDateIsInFuture() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        commonOperations
            .paidInFull(createdCase.getExternalId(), new PaidInFull(now().plusWeeks(1)), claimant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldNoAllowSubmitPaidInFullIfAlreadyDone() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        PaidInFull paidInFull = new PaidInFull(now());

        Claim updatedCase = commonOperations
            .paidInFull(createdCase.getExternalId(), paidInFull, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getMoneyReceivedOn())
            .contains(paidInFull.getMoneyReceivedOn());

        commonOperations
            .paidInFull(createdCase.getExternalId(), paidInFull, claimant)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldNotAllowPaidInFullWhenClaimDoesNotExist() {
        commonOperations
            .paidInFull(UUID.randomUUID().toString(), new PaidInFull(now()), claimant)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

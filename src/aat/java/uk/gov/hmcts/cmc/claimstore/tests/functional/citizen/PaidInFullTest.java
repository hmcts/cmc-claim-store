package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaidInFullTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldSuccessfullySavePaidInFull() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        PaidInFull paidInFull = new PaidInFull(LocalDate.now());

        Claim updatedCase = commonOperations
            .paidInFull(createdCase.getExternalId(), paidInFull, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getMoneyReceivedOn()).isEqualTo(paidInFull);
    }

    @Test(expected = NotFoundException.class)
    public void shouldFailWhenClaimNotFound() {
        String claimantId = claimant.getUserDetails().getId();
        commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        PaidInFull paidInFull = new PaidInFull(LocalDate.now());

        commonOperations
            .paidInFull(UUID.randomUUID().toString(), paidInFull, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

    }
}

package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkDefendantTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());

        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/defendant/link")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value());

        Claim claim = commonOperations.retrieveClaim(createdCase.getExternalId(), claimant.getAuthorisation());

        assertThat(claim.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

}

package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
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
        Claim claim = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

        User defendant = idamTestService.createDefendant(claim.getLetterHolderId());

        linkDefendant(defendant)
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value());

        Claim response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .get("/claims/" + claim.getExternalId())
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(response.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

    private Response linkDefendant(User defendant) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/defendant/link");
    }

}

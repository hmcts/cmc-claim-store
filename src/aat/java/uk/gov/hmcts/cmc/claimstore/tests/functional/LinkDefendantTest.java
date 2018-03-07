package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class LinkDefendantTest extends BaseTest {

    private User claimant;

    @Autowired
    private FunctionalTestsUsers functionalTestsUsers;

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

        User defendant = functionalTestsUsers.createDefendant(claimant.getUserDetails().getId());

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

        assertThat(claim).isEqualTo(response);
    }

    private Response linkDefendant(User defendant) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/defendant/link");
    }

}

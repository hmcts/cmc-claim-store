package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.ccd.assertion.Assertions;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;

public class LinkDefendantTest extends BaseTest {

    @Autowired
    private FunctionalTestsUsers functionalTestsUsers;

    @Test
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        Claim claim = commonOperations.submitClaim(
            functionalTestsUsers.getClaimant().getAuthorisation(),
            functionalTestsUsers.getClaimant().getUserDetails().getId()
        );

        User defendant = functionalTestsUsers.createDefendant();

        linkDefendant(defendant)
            .then()
            .statusCode(HttpStatus.OK.value());

        testCasesRetrievalFor("/claims/defendant/" + defendant.getUserDetails().getId(),
            defendant.getAuthorisation(), claim);
    }

    private Response linkDefendant(User defendant) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/defendant/link");
    }

    public void testCasesRetrievalFor(String uriPath, String authorisation, Claim input) {
        Claim response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, authorisation)
            .when()
            .get(uriPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        Assertions.assertThat(input).isEqualTo(response);
    }
}

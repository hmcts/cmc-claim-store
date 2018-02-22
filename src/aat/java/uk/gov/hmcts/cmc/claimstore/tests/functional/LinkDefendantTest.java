package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkDefendantTest extends BaseTest {

    @Autowired
    private FunctionalTestsUsers functionalTestsUsers;

    @Test
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        Claim createdCase = commonOperations.submitClaim(
            functionalTestsUsers.getClaimant().getAuthorisation(),
            functionalTestsUsers.getClaimant().getUserDetails().getId()
        );

        User defendant = idamTestService.createCitizen();

        Claim claim = linkDefendant(defendant, createdCase.getExternalId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(claim.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

    @Test
    public void shouldReturnNotFoundResponseWhenGivenInvalidClaimExternalReference() {
        User defendant = idamTestService.createCitizen();

        String invalidCaseReference = UUID.randomUUID().toString();

        linkDefendant(defendant, invalidCaseReference)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Response linkDefendant(User defendant, String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/" + externalId + "/defendant/" + defendant.getUserDetails().getId());
    }


}

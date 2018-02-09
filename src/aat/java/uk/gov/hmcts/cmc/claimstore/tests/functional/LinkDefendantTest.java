package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkDefendantTest extends BaseTest {

    @Test
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        saveClaim(claimData)
            .andReturn();

        User defendant = idamTestService.createDefendant();

        Claim claim = linkDefendant(defendant, externalId.toString())
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(claim.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

    private Response linkDefendant(User defendant, String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .when()
            .put("/claims/" + externalId + "/defendant/" + defendant.getUserDetails().getId());
    }


}

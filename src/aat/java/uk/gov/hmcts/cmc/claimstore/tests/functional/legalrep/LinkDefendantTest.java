package uk.gov.hmcts.cmc.claimstore.tests.functional.legalrep;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseLegalRepTest;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkDefendantTest extends BaseLegalRepTest {

    private User solicitor;

    @Before
    public void before() {
        solicitor = idamTestService.createSolicitor();
    }

    @Test
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        Claim createdCase = commonOperations.submitClaim(solicitor.getAuthorisation(),
                                                         solicitor.getUserDetails().getId());

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());

        RestAssured.given()
                   .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
                   .when()
                   .put("/claims/defendant/link")
                   .then()
                   .assertThat()
                   .statusCode(HttpStatus.OK.value());

        Claim claim = commonOperations.retrieveClaim(createdCase.getExternalId(), solicitor.getAuthorisation());

        assertThat(claim.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

}

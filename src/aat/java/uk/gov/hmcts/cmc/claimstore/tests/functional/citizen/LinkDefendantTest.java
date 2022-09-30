package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import springfox.documentation.annotations.Cacheable;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkDefendantTest extends BaseTest {

    private User claimant;

    private static final Logger logger = LoggerFactory.getLogger(LinkDefendantTest.class);

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();
        logger.info("Retrieving claimant details {}", claimant);
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    @Cacheable("user-details")
    public void shouldBeAbleToSuccessfullyLinkDefendant() {
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

        logger.info("Claimant details retrieved,Created case with Authorization: {} and UserID: {}", claimant.getAuthorisation(), claimant.getUserDetails().getId());

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());

        logger.info("Calling Idam test services with Letter Holder ID {}", createdCase.getLetterHolderId());

        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .header("LetterHolderID", createdCase.getLetterHolderId())
            .when()
            .put("/claims/defendant/link")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value());

        logger.info("Linking defendant to claim {} with Letter Holder ID", createdCase.getLetterHolderId());
        Claim claim = commonOperations.retrieveClaim(createdCase.getExternalId(), claimant.getAuthorisation());

        assertThat(claim.getDefendantId()).isEqualTo(defendant.getUserDetails().getId());
    }

}

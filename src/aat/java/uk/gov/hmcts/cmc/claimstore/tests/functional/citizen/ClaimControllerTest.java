package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefendantLinkStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimControllerTest extends BaseTest {

    private User claimant;
    Claim createdCase;

    @Before
    public void before() {
        claimant = bootstrap.getClaimant();

        createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullygetTheStatusOFLinkedClaim() {
        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
        commonOperations.linkDefendant(defendant.getAuthorisation(), createdCase.getLetterHolderId());
        DefendantLinkStatus linkStatus = RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION,  claimant.getAuthorisation())
            .when()
            .get("/claims/" + createdCase.getReferenceNumber() + "/defendant-link-status")
            .as(DefendantLinkStatus.class);

        assertThat(linkStatus)
            .isNotNull()
            .matches(DefendantLinkStatus::isLinked);
    }

    @Test
    @Retry
    public void shouldBeAbleToSuccessfullygetTheClaimByClaimReference() {
        Claim claim = RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION,  claimant.getAuthorisation())
            .when()
            .get("/claims/" + createdCase.getReferenceNumber())
            .as(Claim.class);
        assertThat(claim.getReferenceNumber()).isEqualTo(createdCase.getReferenceNumber());
        assertThat(claim.getResponse()).isEqualTo(createdCase.getResponse());
    }
}

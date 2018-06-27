package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeline;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.temporal.ChronoUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SubmitClaimTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
        ClaimData claimData = testData.submittedByClaimantBuilder().build();
        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), claimant.getAuthorisation());

        Claim createdCase = submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(claimData).isEqualTo(createdCase.getClaimData());
        assertThat(createdCase.getCreatedAt()).isCloseTo(LocalDateTimeFactory.nowInLocalZone(),
            within(2, ChronoUnit.MINUTES));
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidClaimIsSubmitted() {
        ClaimData claimData = testData.submittedByClaimantBuilder().withAmount(null).build();
        submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
        ClaimData claimData = testData.submittedByClaimantBuilder().build();

        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), claimant.getAuthorisation());

        submitClaim(claimData)
            .andReturn();
        submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidTimelineIsSubmitted() {
        ClaimData invalidClaimData = testData.submittedByClaimantBuilder()
            .withTimeline(SampleTimeline.builder().withEvents(asList(new TimelineEvent[1001])).build())
            .build();

        submitClaim(invalidClaimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidEvidenceIsSubmitted() {
        ClaimData invalidClaimData = testData.submittedByClaimantBuilder()
            .withEvidence(SampleEvidence.builder().withRows(asList(new EvidenceRow[1001])).build())
            .build();

        submitClaim(invalidClaimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    private Response submitClaim(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + claimant.getUserDetails().getId());
    }

}

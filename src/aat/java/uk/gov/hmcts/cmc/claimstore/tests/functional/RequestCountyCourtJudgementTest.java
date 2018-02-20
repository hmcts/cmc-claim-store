package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RequestCountyCourtJudgementTest extends BaseTest {

    @Test
    public void shouldBeAbleToSuccessfullyRequestCCJ() {
        Claim createdCase = commonOperations.submitClaim(
            bootstrap.getCitizenUser().getAuthorisation(),
            bootstrap.getCitizenUser().getUserDetails().getId()
        );

        updateResponseDeadlineToEnableCCJ(createdCase.getReferenceNumber());

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        Claim updatedCase = requestCCJ(createdCase.getExternalId(), ccj)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getCountyCourtJudgment()).isEqualTo(ccj);
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt())
            .isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidJudgementIsSubmitted() {
        Claim createdCase = commonOperations.submitClaim(
            bootstrap.getCitizenUser().getAuthorisation(),
            bootstrap.getCitizenUser().getUserDetails().getId()
        );

        updateResponseDeadlineToEnableCCJ(createdCase.getReferenceNumber());

        CountyCourtJudgment invalidCCJ = SampleCountyCourtJudgment.builder()
            .withPaymentOption(null)
            .build();

        requestCCJ(createdCase.getExternalId(), invalidCCJ)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldNotBeAllowedToRequestCCJWhenResponseDeadlineHasNotPassed() {
        Claim createdCase = commonOperations.submitClaim(
            bootstrap.getCitizenUser().getAuthorisation(),
            bootstrap.getCitizenUser().getUserDetails().getId()
        );

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        requestCCJ(createdCase.getExternalId(), ccj)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private Response requestCCJ(String externalId, CountyCourtJudgment ccj) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getCitizenUser().getAuthorisation())
            .body(jsonMapper.toJson(ccj))
            .when()
            .post("/claims/" + externalId + "/county-court-judgment");
    }

    private void updateResponseDeadlineToEnableCCJ(String claimReferenceNumber) {
        String updatedDeadlineString = LocalDate.now().minusMonths(1).toString();
        RestAssured
            .put("/testing-support/claims/" + claimReferenceNumber + "/response-deadline/" + updatedDeadlineString);
    }


}

package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CountyCourtJudgementTest extends BaseTest {

    private User claimant;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToSuccessfullyRequestCCJ() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        updateResponseDeadlineToEnableCCJ(createdCase.getReferenceNumber());

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        Claim updatedCase = commonOperations.requestCCJ(createdCase.getExternalId(), ccj, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getCountyCourtJudgment()).isEqualTo(ccj);
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt())
            .isCloseTo(LocalDateTimeFactory.nowInLocalZone(), within(2, ChronoUnit.MINUTES));
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidJudgementIsSubmitted() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        updateResponseDeadlineToEnableCCJ(createdCase.getReferenceNumber());

        CountyCourtJudgment invalidCCJ = SampleCountyCourtJudgment.builder()
            .paymentOption(null)
            .build();

        commonOperations.requestCCJ(createdCase.getExternalId(), invalidCCJ, claimant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldNotBeAllowedToDefaultCCJWhenResponseDeadlineHasNotPassed() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        io.restassured.response.Response response = commonOperations
            .requestCCJ(createdCase.getExternalId(), ccj, claimant);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void shouldNotBeAllowedToRequestCCJWhenResponseWasSubmitted() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createCitizen();
        commonOperations.linkDefendant(defendant.getAuthorisation());

        Response response = SampleResponse.PartAdmission.validDefaults();
        commonOperations.submitResponse(response, createdCase.getExternalId(), defendant);

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        commonOperations.requestCCJ(createdCase.getExternalId(), ccj, claimant)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private void updateResponseDeadlineToEnableCCJ(String claimReferenceNumber) {
        String updatedDeadlineString = LocalDate.now().minusMonths(1).toString();
        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .put("/testing-support/claims/" + claimReferenceNumber + "/response-deadline/" + updatedDeadlineString)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

}

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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.FullAdmission;
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
            .withPaymentOptionImmediately()
            .build();

        Claim updatedCase = commonOperations.requestCCJ(createdCase.getExternalId(), ccj, false, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getCountyCourtJudgment()).isEqualTo(ccj);
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt())
            .isCloseTo(LocalDateTimeFactory.nowInLocalZone(), within(2, ChronoUnit.MINUTES));
    }

    @Test
    public void shouldBeAbleToSuccessfullyIssueCCJ() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        uk.gov.hmcts.cmc.domain.models.response.Response fullAdmissionResponse = FullAdmission.builder().build();

        String externalId = createdCase.getExternalId();
        commonOperations.submitResponse(fullAdmissionResponse, externalId, defendant)
            .then()
            .statusCode(HttpStatus.OK.value());

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        Claim updatedCase = commonOperations.requestCCJ(externalId, ccj, true, claimant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getCountyCourtJudgment()).isEqualTo(ccj);
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt())
            .isCloseTo(LocalDateTimeFactory.nowInLocalZone(), within(2, ChronoUnit.MINUTES));

        assertThat(updatedCase.getCountyCourtJudgmentIssuedAt().isPresent()).isTrue();

        assertThat(updatedCase.getCountyCourtJudgmentIssuedAt().get())
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
            .withPaymentOption(null)
            .build();

        commonOperations.requestCCJ(createdCase.getExternalId(), invalidCCJ, false, claimant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldNotBeAllowedToRequestCCJWhenResponseDeadlineHasNotPassed() {
        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        commonOperations.requestCCJ(createdCase.getExternalId(), ccj, false, claimant)
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

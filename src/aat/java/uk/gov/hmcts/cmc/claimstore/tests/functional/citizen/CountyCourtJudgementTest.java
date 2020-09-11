package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CountyCourtJudgementTest extends BaseTest {

    private static User claimant;
    private static String claimantId;
    private static Claim createdCase;
    private static User defendant;
    private static Claim updatedCase;
    private static Offer offer;

    @BeforeClass
    public static void beforeClass() {
        CountyCourtJudgementTest countyCourtJudgementTest = new CountyCourtJudgementTest();
        claimant = countyCourtJudgementTest.bootstrap.getClaimant();
        claimantId = claimant.getUserDetails().getId();
        createdCase = countyCourtJudgementTest.commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );
    }

    @Test
    public void shouldBeAbleToSuccessfullyRequestCCJ() {
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
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt()).isNotNull();
    }

    @Test
    public void shouldNotBeAllowedToDefaultCCJWhenResponseDeadlineHasNotPassed() {
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

        User defendant = idamTestService.upliftDefendant(createdCase.getLetterHolderId(), bootstrap.getDefendant());
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

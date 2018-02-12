package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SaveCountyCourtJudgementTest extends BaseTest {

    @Test
    public void shouldBeAbleToSuccessfullyRequestCCJ() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        Claim createdCase = saveClaim(claimData)
            .then().extract().body().as(Claim.class);

        updateResponseDeadlineToEnableCCJ(createdCase.getReferenceNumber());

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        Claim updatedCase = requestCCJ(externalId.toString(), ccj)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getCountyCourtJudgment()).isEqualTo(ccj);
        assertThat(updatedCase.getCountyCourtJudgmentRequestedAt()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }

    @Test
    public void shouldNotBeAllowedToRequestCCJWhenResponseDeadlineHasNotPassed() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        saveClaim(claimData)
            .andReturn();

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .build();

        requestCCJ(externalId.toString(), ccj)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private Response requestCCJ(String externalId, CountyCourtJudgment ccj) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getUserAuthenticationToken())
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

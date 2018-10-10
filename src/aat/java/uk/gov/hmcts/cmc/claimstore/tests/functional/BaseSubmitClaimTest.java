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
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public abstract class BaseSubmitClaimTest extends BaseTest {
    protected User user;

    protected Response submitClaim(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + user.getUserDetails().getId());
    }

    // FAILS
    @Test
    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
        ClaimData claimData = getSampleClaimDataBuilder().get().build();
        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), user.getAuthorisation());

        Claim createdCase = submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(claimData).isEqualTo(createdCase.getClaimData());
        assertThat(createdCase.getCreatedAt()).isCloseTo(LocalDateTimeFactory.nowInLocalZone(),
            within(2, ChronoUnit.MINUTES));
    }

    // FAILS
    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidClaimIsSubmitted() {
        ClaimData claimData = getSampleClaimDataBuilder().get().withAmount(null).build();
        submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    // FAILS
    @Test
    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
        ClaimData claimData = getSampleClaimDataBuilder().get().build();

        commonOperations.submitPrePaymentClaim(claimData.getExternalId().toString(), user.getAuthorisation());

        submitClaim(claimData)
            .andReturn();
        submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    protected abstract Supplier<SampleClaimData> getSampleClaimDataBuilder();
}

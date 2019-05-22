package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import io.restassured.RestAssured;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Service
public class ClaimOperation {

    @Retryable(value = RuntimeException.class, maxAttempts = 15, backoff = @Backoff(delay = 500, maxDelay = 3000))
    public Claim getClaimWithLetterHolder(String externalId, String userAuthentication) {

        Claim claim = retrieveClaim(externalId, userAuthentication);
        if (!Optional.ofNullable(claim.getLetterHolderId()).isPresent()) {
            throw new RuntimeException("pin process not complete");
        }
        return claim;
    }

    @Recover
    public Claim recover(RuntimeException e, String externalId, String userAuthentication) {
        // do nothing
        throw new RuntimeException("Exhausted all retries, pin process not complete, please try again");
    }

    public Claim retrieveClaim(String externalId, String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .get(String.format("claims/%s", externalId))
            .then()
            .extract()
            .body()
            .as(Claim.class);
    }
}

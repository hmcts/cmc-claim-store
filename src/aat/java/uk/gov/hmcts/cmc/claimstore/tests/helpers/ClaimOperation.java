package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import io.restassured.RestAssured;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.models.CaseMetadata;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.Optional;

@Service
public class ClaimOperation {

    @Retryable(value = RuntimeException.class, maxAttempts = 20, backoff = @Backoff(delay = 500))
    public Claim getClaimWithLetterHolder(String externalId, String userAuthentication) {

        Optional<CaseMetadata> caseMetadata = Optional.of(retrieveCaseMetaData(externalId));
        if (! caseMetadata.filter(c -> c.getState() == ClaimState.OPEN).isPresent()) {
            throw new RuntimeException("pin process not complete");
        }

        Claim claim = retrieveClaim(externalId, userAuthentication);
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

    public CaseMetadata retrieveCaseMetaData(String externalId) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(String.format("claims/%s/metadata", externalId))
            .then()
            .extract()
            .body()
            .as(CaseMetadata.class);
    }
}

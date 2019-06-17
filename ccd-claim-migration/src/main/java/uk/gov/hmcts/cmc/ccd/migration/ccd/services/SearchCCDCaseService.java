package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SearchCCDCaseService {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private static final Logger logger = LoggerFactory.getLogger(SearchCCDCaseService.class);

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SearchCCDCaseService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Retryable(include = {SocketTimeoutException.class, FeignException.class, IOException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 400, maxDelay = 800)
    )
    @LogExecutionTime
    public Optional<CaseDetails> getCcdCaseByReferenceNumber(User user, String referenceNumber) {
        return search(user, ImmutableMap.of("case.previousServiceCaseReference", referenceNumber));
    }

    @Retryable(include = {SocketTimeoutException.class, FeignException.class, IOException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 400, maxDelay = 800)
    )
    public Optional<CaseDetails> getCcdCaseByExternalId(User user, String externalId) {
        return search(user, ImmutableMap.of("case.externalId", externalId));
    }

    @Recover
    public Optional<CaseDetails> recoverSearchFailure(RuntimeException exception, User user, String externalId) {
        String errorMessage = String.format(
            "Failure: failed search by reference number ( %s for user %s ) due to %s",
            externalId, user.getUserDetails().getId(), exception.getMessage()
        );

        logger.info(errorMessage, exception);

        return Optional.empty();
    }

    private Optional<CaseDetails> search(User user, Map<String, String> searchString) {

        List<CaseDetails> result;
        result = this.coreCaseDataApi.searchForCaseworker(
            user.getAuthorisation(),
            this.authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            searchString
        );

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}

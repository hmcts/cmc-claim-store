package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventDetails;
import uk.gov.hmcts.cmc.ccd.migration.client.CaseEventsApi;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

@Service
public class SearchCCDEventsService {

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private static final Logger logger = LoggerFactory.getLogger(SearchCCDEventsService.class);

    private final CaseEventsApi caseEventsApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SearchCCDEventsService(
        CaseEventsApi caseEventsApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.caseEventsApi = caseEventsApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Retryable(include = {SocketTimeoutException.class, FeignException.class, IOException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 400, maxDelay = 800)
    )
    public List<CaseEventDetails> getCcdCaseEventsForCase(User user, String caseId) {
        return this.caseEventsApi.findEventDetailsForCase(
            user.getAuthorisation(),
            this.authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId
        );
    }

    @Recover
    public List<CaseEventDetails> recoverCaseEventsFailure(RuntimeException exception, User user, String caseId) {
        String errorMessage = String.format(
            "Failure: failed search by reference number ( %s for user %s ) due to %s",
            caseId, user.getUserDetails().getId(), exception.getMessage()
        );

        logger.info(errorMessage, exception);
        return null;
    }
}

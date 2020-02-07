package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.CreateCaseException;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

@Service
public class CreateCCDCaseService {
    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private static final Logger logger = LoggerFactory.getLogger(CreateCCDCaseService.class);

    private final MigrateCoreCaseDataService migrateCoreCaseDataService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public CreateCCDCaseService(
        MigrateCoreCaseDataService migrateCoreCaseDataService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.migrateCoreCaseDataService = migrateCoreCaseDataService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Retryable(value = {CreateCaseException.class}, backoff = @Backoff(delay = 400, maxDelay = 800), maxAttempts = 5)
    @LogExecutionTime
    public CaseDetails createCase(User user, Claim claim, CaseEvent event) {
        try {

            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(event.getValue())
                .ignoreWarning(true)
                .build();

            return migrateCoreCaseDataService.save(user.getAuthorisation(), eventRequestData, claim);
        } catch (Exception exception) {
            throw new CreateCaseException(
                String.format("Failed storing claim in CCD store for claim on %s on event %s due to %s",
                    claim.getReferenceNumber(),
                    event.getValue(),
                    exception.getMessage()
                ),
                exception
            );
        }
    }

    @Recover
    public CaseDetails recoverSaveFailure(
        CreateCaseException exception,
        User user,
        Claim claim,
        CaseEvent event
    ) {
        logger.info(exception.getMessage(), exception);
        throw exception;
    }
}

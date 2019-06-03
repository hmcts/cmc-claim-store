package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions.OverwriteCaseException;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

@Service
public class UpdateCCDCaseService {
    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private static final Logger logger = LoggerFactory.getLogger(UpdateCCDCaseService.class);

    private final MigrateCoreCaseDataService migrateCoreCaseDataService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public UpdateCCDCaseService(
        MigrateCoreCaseDataService migrateCoreCaseDataService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.migrateCoreCaseDataService = migrateCoreCaseDataService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Retryable(value = {OverwriteCaseException.class}, maxAttempts = 5, backoff = @Backoff(delay = 400, maxDelay = 800))
    @LogExecutionTime
    public CaseDetails updateCase(User user, Long caseId, Claim claim, CaseEvent event) {

        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(event.getValue())
                .ignoreWarning(true)
                .build();

            return migrateCoreCaseDataService.update(user.getAuthorisation(), eventRequestData, caseId, claim);
        } catch (Exception exception) {
            throw new OverwriteCaseException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s due to %s",
                    claim.getReferenceNumber(),
                    event,
                    exception.getMessage()
                ),
                exception
            );
        }
    }

    @Recover
    public CaseDetails recoverUpdateFailure(
        OverwriteCaseException exception,
        User user,
        Long caseId,
        Claim claim,
        CaseEvent event
    ) {
        logger.info(exception.getMessage(), exception);
        throw exception;
    }

}

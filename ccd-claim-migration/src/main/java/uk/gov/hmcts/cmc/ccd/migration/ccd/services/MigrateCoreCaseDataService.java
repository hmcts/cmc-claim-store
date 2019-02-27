package uk.gov.hmcts.cmc.ccd.migration.ccd.services;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.mappers.JsonMapper;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class MigrateCoreCaseDataService {
    private static final Logger logger = LoggerFactory.getLogger(MigrateCoreCaseDataService.class);

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final UserService userService;
    private final CaseMapper caseMapper;
    private final JsonMapper jsonMapper;

    @Autowired
    public MigrateCoreCaseDataService(
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        UserService userService,
        CaseMapper caseMapper,
        JsonMapper jsonMapper
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseAccessApi = caseAccessApi;
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.jsonMapper = jsonMapper;
    }

    @Retryable(value = {SocketTimeoutException.class, FeignException.class, IOException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 400, maxDelay = 800)
    )
    public void update(
        String authorisation,
        EventRequestData eventRequestData,
        Long ccdId,
        Claim claim
    ) {
        CCDCase ccdCase = caseMapper.to(claim);

        StartEventResponse startEventResponse = startEvent(authorisation, eventRequestData, ccdId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build()
            ).data(ccdCase)
            .build();

        submitEvent(authorisation, eventRequestData, caseDataContent, ccdId);
    }

    @Recover
    public void recoverUpdateFailure(
        SocketTimeoutException exception,
        String authorisation,
        EventRequestData eventRequestData,
        Long ccdId,
        Claim claim
    ) {
        String errorMessage = String.format(
            "Failure: failed update for reference number ( %s for event %s) due to %s",
            claim.getReferenceNumber(), eventRequestData.getEventId(), exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    @Retryable(value = {SocketTimeoutException.class, FeignException.class, IOException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 400, maxDelay = 800)
    )
    public CaseDetails save(String authorisation, EventRequestData eventRequestData, Claim claim) {
        CCDCase ccdCase = caseMapper.to(claim);

        StartEventResponse startEventResponse = start(authorisation, eventRequestData);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case submission event")
                    .description("Submitting CMC pre-payment case")
                    .build()
            )
            .data(ccdCase)
            .build();

        CaseDetails caseDetails = submit(authorisation, eventRequestData, caseDataContent);

        grantAccessToCase(caseDetails.getId(), claim);
        return caseDetails;
    }

    private CCDCase extractCase(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseDetails.getId());
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }

    @Recover
    public void recoverSaveFailure(
        SocketTimeoutException exception,
        String authorisation,
        EventRequestData eventRequestData,
        Claim claim
    ) {
        String errorMessage = String.format(
            "Failure: failed save for reference number ( %s for event %s) due to %s",
            claim.getReferenceNumber(), eventRequestData.getEventId(), exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    private void grantAccessToCase(Long ccdId, Claim claim) {
        // make sure both submitter and defendant (or letterHolder) can access the case
        grantAccess(ccdId.toString(), claim.getSubmitterId());
        // only claims created by citizen have this fields populated
        if (!claim.getClaimData().isClaimantRepresented()) {
            String defendantId = claim.getDefendantId() != null ? claim.getDefendantId() : claim.getLetterHolderId();
            grantAccess(ccdId.toString(), defendantId);
        }

    }

    @LogExecutionTime
    public void grantAccess(
        String caseId, String userId
    ) {
        User user = userService.authenticateAnonymousCaseWorker();

        caseAccessApi.grantAccessToCase(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseId,
            new UserId(userId)
        );
    }

    @LogExecutionTime
    public CaseDetails submit(
        String authorisation, EventRequestData eventRequestData, CaseDataContent caseDataContent
    ) {
        return coreCaseDataApi.submitForCaseworker(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }

    @LogExecutionTime
    public StartEventResponse start(String authorisation, EventRequestData eventRequestData) {
        return this.coreCaseDataApi.startForCaseworker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );
    }

    @LogExecutionTime
    public StartEventResponse startEvent(String authorisation, EventRequestData eventRequestData, Long caseId) {

        return this.coreCaseDataApi.startEventForCaseWorker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.getEventId()
        );
    }

    @LogExecutionTime
    public CaseDetails submitEvent(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        Long caseId
    ) {

        return this.coreCaseDataApi.submitEventForCaseWorker(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            caseId.toString(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }
}

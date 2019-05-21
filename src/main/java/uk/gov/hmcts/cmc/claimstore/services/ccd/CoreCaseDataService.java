package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseEventMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_LETTER_HOLDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_ONLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_enabled", havingValue = "true")
public class CoreCaseDataService {

    public static final String CMC_CASE_UPDATE_SUMMARY = "CMC case update";
    public static final String CMC_CASE_CREATE_SUMMARY = "CMC case issue";
    public static final String SUBMITTING_CMC_CASE_UPDATE_DESCRIPTION = "Submitting CMC case update";
    public static final String SUBMITTING_CMC_CASE_ISSUE_DESCRIPTION = "Submitting CMC case issue";

    public static final String CCD_UPDATE_FAILURE_MESSAGE
        = "Failed updating claim in CCD store for case id %s on event %s";

    public static final String CCD_STORING_FAILURE_MESSAGE
        = "Failed storing claim in CCD store for case id %s on event %s";

    private final CaseMapper caseMapper;
    private final UserService userService;
    private final JsonMapper jsonMapper;
    private final ReferenceNumberService referenceNumberService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final JobSchedulerService jobSchedulerService;
    private final CCDCreateCaseService ccdCreateCaseService;

    @SuppressWarnings("squid:S00107") // All parameters are required here
    @Autowired
    public CoreCaseDataService(
        CaseMapper caseMapper,
        UserService userService,
        JsonMapper jsonMapper,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        JobSchedulerService jobSchedulerService,
        CCDCreateCaseService ccdCreateCaseService
    ) {
        this.caseMapper = caseMapper;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.referenceNumberService = referenceNumberService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.jobSchedulerService = jobSchedulerService;
        this.ccdCreateCaseService = ccdCreateCaseService;
    }

    @LogExecutionTime
    public Claim createNewCase(User user, Claim claim) {
        requireNonNull(user, "user must not be null");

        CCDCase ccdCase = caseMapper.to(claim);

        if (StringUtils.isBlank(claim.getReferenceNumber())) {
            ccdCase.setPreviousServiceCaseReference(referenceNumberService.getReferenceNumber(user.isRepresented()));
        }

        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(CREATE_CASE.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = ccdCreateCaseService.startCreate(user.getAuthorisation(),
                eventRequestData, user.isRepresented());

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(CMC_CASE_CREATE_SUMMARY)
                    .description(SUBMITTING_CMC_CASE_ISSUE_DESCRIPTION)
                    .build())
                .data(ccdCase)
                .build();

            CaseDetails caseDetails = ccdCreateCaseService.submitCreate(
                user.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                user.isRepresented()
            );

            if (!user.isRepresented() && StringUtils.isNotBlank(claim.getLetterHolderId())) {
                ccdCreateCaseService.grantAccessToCase(caseDetails.getId().toString(), claim.getLetterHolderId());
            }

            return extractClaim(caseDetails);

        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_STORING_FAILURE_MESSAGE,
                    ccdCase.getPreviousServiceCaseReference(),
                    CREATE_CASE
                ), exception
            );
        }
    }

    public Claim requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        Long caseId = claim.getId();
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(MORE_TIME_REQUESTED_ONLINE, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .responseDeadline(newResponseDeadline)
                .moreTimeRequested(true)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            jobSchedulerService.rescheduleEmailNotificationsForDefendantResponse(claim, newResponseDeadline);
            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    MORE_TIME_REQUESTED_ONLINE
                ), exception
            );
        }
    }

    public CaseDetails saveCountyCourtJudgment(
        String authorisation,
        Long caseId,
        CountyCourtJudgment countyCourtJudgment
    ) {
        CaseEvent caseEvent = getCCJEvent(countyCourtJudgment.getCcjType());
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .countyCourtJudgment(countyCourtJudgment)
                .countyCourtJudgmentRequestedAt(nowInUTC())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }

    }

    private CaseEvent getCCJEvent(CountyCourtJudgmentType countyCourtJudgmentType) {
        if (countyCourtJudgmentType == CountyCourtJudgmentType.ADMISSIONS
            || countyCourtJudgmentType == CountyCourtJudgmentType.DETERMINATION
        ) {
            return CCJ_REQUESTED;
        } else {
            return DEFAULT_CCJ_REQUESTED;
        }
    }

    public Claim saveClaimDocuments(
        String authorisation,
        Long caseId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(
                CaseEventMapper.map(claimDocumentType), userDetails.getId()
            );

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .claimDocumentCollection(claimDocumentCollection)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    CaseEventMapper.map(claimDocumentType)
                ), exception
            );
        }
    }

    public CaseDetails saveDefendantResponse(
        Long caseId,
        String defendantEmail,
        Response response,
        String authorisation
    ) {
        CaseEvent caseEvent = CaseEvent.valueOf(getResponseTypeName(response));

        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .response(response)
                .defendantEmail(defendantEmail)
                .respondedAt(nowInUTC())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    private String getResponseTypeName(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                return ((FullDefenceResponse) response).getDefenceType().name();
            case FULL_ADMISSION:
            case PART_ADMISSION:
                return response.getResponseType().name();
            default:
                throw new IllegalArgumentException("Invalid response type " + response.getResponseType());

        }
    }

    public Claim saveClaimantResponse(
        Long caseId,
        ClaimantResponse response,
        String authorisation
    ) {
        CaseEvent caseEvent = CaseEvent.valueOf("CLAIMANT_RESPONSE_" + response.getType().name());

        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .claimantResponse(response)
                .claimantRespondedAt(nowInUTC())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return extractClaim(submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
                )
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    public CaseDetails saveSettlement(
        Long caseId,
        Settlement settlement,
        String authorisation,
        CaseEvent caseEvent
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .settlement(settlement)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    public CaseDetails reachSettlementAgreement(
        Long caseId,
        Settlement settlement,
        LocalDateTime settlementReachedAt,
        String authorisation,
        CaseEvent caseEvent
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .settlement(settlement)
                .settlementReachedAt(settlementReachedAt)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    public CaseDetails updateResponseDeadline(
        String authorisation,
        Long caseId,
        LocalDate newResponseDeadline
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(TEST_SUPPORT_UPDATE, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .responseDeadline(newResponseDeadline)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    TEST_SUPPORT_UPDATE
                ), exception
            );
        }
    }

    public CaseDetails linkDefendant(
        String authorisation,
        Long caseId,
        String defendantId,
        String defendantEmail,
        CaseEvent caseEvent
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .defendantEmail(defendantEmail)
                .defendantId(defendantId)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    private Claim.ClaimBuilder toClaimBuilder(StartEventResponse startEventResponse) {
        CCDCase ccdCase = extractCase(startEventResponse.getCaseDetails());
        Claim claim = caseMapper.from(ccdCase);
        return claim.toBuilder();
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Claim ccdClaim) {
        return caseDataContent(startEventResponse, caseMapper.to(ccdClaim));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, CCDCase ccdCase) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(CMC_CASE_UPDATE_SUMMARY)
                .description(SUBMITTING_CMC_CASE_UPDATE_DESCRIPTION)
                .build())
            .data(ccdCase)
            .build();
    }

    private EventRequestData eventRequest(CaseEvent caseEvent, String userId) {
        return EventRequestData.builder()
            .userId(userId)
            .jurisdictionId(JURISDICTION_ID)
            .caseTypeId(CASE_TYPE_ID)
            .eventId(caseEvent.getValue())
            .ignoreWarning(true)
            .build();
    }

    public CaseDetails update(String authorisation, CCDCase ccdCase, CaseEvent caseEvent) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            Long caseId = ccdCase.getId();
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(userDetails.getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(caseEvent.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(CMC_CASE_UPDATE_SUMMARY)
                    .description(SUBMITTING_CMC_CASE_UPDATE_DESCRIPTION)
                    .build())
                .data(ccdCase)
                .build();

            return submitUpdate(authorisation, eventRequestData, caseDataContent, caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s",
                    ccdCase.getPreviousServiceCaseReference(),
                    caseEvent
                ), exception
            );
        }
    }

    private StartEventResponse startUpdate(
        String authorisation,
        EventRequestData eventRequestData,
        Long caseId,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.getEventId()
            );
        } else {
            return coreCaseDataApi.startEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.getEventId()
            );
        }
    }

    private CaseDetails submitUpdate(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        Long caseId,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        } else {
            return coreCaseDataApi.submitEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }
    }

    private Claim extractClaim(CaseDetails caseDetails) {
        return caseMapper.from(extractCase(caseDetails));
    }

    private CCDCase extractCase(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseDetails.getId());
        return jsonMapper.fromMap(caseData, CCDCase.class);
    }

    public void saveDirectionsQuestionnaireDeadline(Long caseId, LocalDate dqDeadline, String authorisation) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(DIRECTIONS_QUESTIONNAIRE_DEADLINE, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .directionsQuestionnaireDeadline(dqDeadline)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    DIRECTIONS_QUESTIONNAIRE_DEADLINE
                ), exception
            );
        }
    }

    public void saveCaseEvent(String authorisation, Long caseId, CaseEvent caseEvent) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            CCDCase ccdCase = extractCase(startEventResponse.getCaseDetails());

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, ccdCase);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    public void saveReDetermination(
        String authorisation,
        Long caseId,
        ReDetermination reDetermination,
        CaseEvent event
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(event, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .reDetermination(reDetermination)
                .reDeterminationRequestedAt(nowInUTC())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    event
                ), exception
            );
        }
    }

    public void savePaidInFull(Long caseId, PaidInFull paidInFull, String authorisation) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(SETTLED_PRE_JUDGMENT, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .moneyReceivedOn(paidInFull.getMoneyReceivedOn())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    SETTLED_PRE_JUDGMENT
                ), exception
            );
        }
    }

    public Claim saveClaimSubmissionOperationIndicators(Long caseId,
                                                        ClaimSubmissionOperationIndicators indicators,
                                                        String authorisation,
                                                        CaseEvent caseEvent) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .claimSubmissionOperationIndicators(indicators)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    public Claim linkLetterHolder(Long caseId, String letterHolderId) {
        try {
            User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();

            UserDetails userDetails = anonymousCaseWorker.getUserDetails();
            EventRequestData eventRequestData = eventRequest(LINK_LETTER_HOLDER, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                anonymousCaseWorker.getAuthorisation(),
                eventRequestData,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            CCDCase ccdCase = extractCase(startEventResponse.getCaseDetails());
            Claim claim = caseMapper.from(ccdCase);

            Claim updatedClaim = claim.toBuilder()
                .letterHolderId(letterHolderId)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(
                anonymousCaseWorker.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker()
            );

            ccdCreateCaseService.grantAccessToCase(caseId.toString(), letterHolderId);
            Optional.ofNullable(claim.getLetterHolderId()).ifPresent(
                previousLetterHolderId ->
                    ccdCreateCaseService.removeAccessToCase(caseId.toString(), previousLetterHolderId)
            );

            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    LINK_LETTER_HOLDER
                ), exception
            );
        }
    }
}

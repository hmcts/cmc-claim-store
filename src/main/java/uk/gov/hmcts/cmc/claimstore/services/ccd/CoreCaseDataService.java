package uk.gov.hmcts.cmc.claimstore.services.ccd;

import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpaceType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseEventMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.StateTransitionCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
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
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.*;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot.JDDO;
import static uk.gov.hmcts.cmc.claimstore.services.pilotcourt.Pilot.LA;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {
    private static final String CMC_CASE_UPDATE_SUMMARY = "CMC case update";
    private static final String CMC_CASE_CREATE_SUMMARY = "CMC case create";
    private static final String CMC_PAYMENT_CREATE_SUMMARY = "CMC payment creation";
    private static final String SUBMITTING_CMC_CASE_UPDATE_DESCRIPTION = "Submitting CMC case update";
    private static final String SUBMITTING_CMC_CASE_CREATE_DESCRIPTION = "Submitting CMC case create";
    private static final String SUBMITTING_CMC_INITIATE_PAYMENT_DESCRIPTION = "Submitting CMC initiate payment";
    private static final String MORE_TIME_DEFENDANT_MSG = "Response Deadline Extended by Defendant";

    private static final String CCD_UPDATE_FAILURE_MESSAGE
        = "Failed updating claim in CCD store for case id %s on event %s";

    private static final String CCD_MORE_TIME_REQUESTED_ONLINE_FAILURE
        = "Failed updating claim in CCD store for case id %s on event %s";

    private static final String CCD_STORING_FAILURE_MESSAGE
        = "Failed storing claim in CCD store for case id %s on event %s";

    private static final String CCD_PAYMENT_CREATE_FAILURE_MESSAGE
        = "Failed creating a payment in CCD store for claim with external id %s on event %s";
    private static final String USER_MUST_NOT_BE_NULL = "user must not be null";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseMapper caseMapper;
    private final UserService userService;
    private final ReferenceNumberService referenceNumberService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CCDCreateCaseService ccdCreateCaseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final WorkingDayIndicator workingDayIndicator;
    private final int intentionToProceedDeadlineDays;
    private final DirectionsQuestionnaireService directionsQuestionnaireService;
    private final PilotCourtService pilotCourtService;

    @SuppressWarnings("squid:S00107") // All parameters are required here
    @Autowired
    public CoreCaseDataService(
        CaseMapper caseMapper,
        UserService userService,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CCDCreateCaseService ccdCreateCaseService,
        CaseDetailsConverter caseDetailsConverter,
        @Value("#{new Integer('${dateCalculations.stayClaimDeadlineInDays}')}")
            Integer intentionToProceedDeadlineDays,
        WorkingDayIndicator workingDayIndicator,
        DirectionsQuestionnaireService directionsQuestionnaireService,
        PilotCourtService pilotCourtService
    ) {
        this.caseMapper = caseMapper;
        this.userService = userService;
        this.referenceNumberService = referenceNumberService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdCreateCaseService = ccdCreateCaseService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.workingDayIndicator = workingDayIndicator;
        this.intentionToProceedDeadlineDays = intentionToProceedDeadlineDays;
        this.directionsQuestionnaireService = directionsQuestionnaireService;
        this.pilotCourtService = pilotCourtService;
    }

    @LogExecutionTime
    public Claim createNewCase(User user, Claim claim) {
        requireNonNull(user, USER_MUST_NOT_BE_NULL);

        CCDCase ccdCase = caseMapper.to(claim);

        if (StringUtils.isBlank(claim.getReferenceNumber())) {
            ccdCase.setPreviousServiceCaseReference(referenceNumberService.getReferenceNumber(user.isRepresented()));
        }

        return saveClaim(user, claim, ccdCase, CREATE_CASE);
    }

    @LogExecutionTime
    public Claim createNewHelpWithFeesCase(User user, Claim claim) {
        requireNonNull(user, USER_MUST_NOT_BE_NULL);

        CCDCase ccdCase = caseMapper.to(claim);

        return saveClaim(user, claim, ccdCase, CREATE_HWF_CASE);
    }

    @LogExecutionTime
    public Claim createRepresentedClaim(User user, Claim claim) {
        requireNonNull(user, USER_MUST_NOT_BE_NULL);

        CCDCase ccdCase = caseMapper.to(claim);

        return saveClaim(user, claim, ccdCase, CREATE_LEGAL_REP_CLAIM);
    }

    @LogExecutionTime
    public Claim updateRepresentedClaim(User user, Claim claim) {
        requireNonNull(user, USER_MUST_NOT_BE_NULL);

        CaseEvent caseEvent = UPDATE_LEGAL_REP_CLAIM;

        try {
            UserDetails userDetails = userService.getUserDetails(user.getAuthorisation());

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                user.getAuthorisation,
                eventRequestData,
                claim.getCcdCaseId(),
                isRepresented(userDetails)
            );

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, claim);

            return caseDetailsConverter.extractClaim(submitUpdate(user.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                claim.getCcdCaseId(),
                isRepresented(userDetails)
                )
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    claim.getCcdCaseId(),
                    caseEvent
                ), exception
            );
        }
    }

    private Claim saveClaim(User user, Claim claim, CCDCase ccdCase, CaseEvent createClaimEvent) {
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(createClaimEvent.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = ccdCreateCaseService.startCreate(user.getAuthorisation(),
                eventRequestData, user.isRepresented());

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(CMC_CASE_CREATE_SUMMARY)
                    .description(SUBMITTING_CMC_CASE_CREATE_DESCRIPTION)
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

            return caseDetailsConverter.extractClaim(caseDetails);
        } catch (FeignException.Conflict conflict) {
            throw new ConflictException("Claim already exists in CCD with externalId " + claim.getExternalId());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_STORING_FAILURE_MESSAGE,
                    ccdCase.getPreviousServiceCaseReference(),
                    createClaimEvent
                ), exception
            );
        }
    }

    @LogExecutionTime
    public Claim initiatePaymentForCitizenCase(
        User user,
        Claim claim
    ) {
        requireNonNull(user, USER_MUST_NOT_BE_NULL);

        CCDCase ccdCase = caseMapper.to(claim);

        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getUserDetails().getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = ccdCreateCaseService.startCreate(user.getAuthorisation(),
                eventRequestData, false);

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(CMC_PAYMENT_CREATE_SUMMARY)
                    .description(SUBMITTING_CMC_INITIATE_PAYMENT_DESCRIPTION)
                    .build())
                .data(ccdCase)
                .build();

            CaseDetails caseDetails = ccdCreateCaseService.submitCreate(
                user.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                false
            );

            return caseDetailsConverter.extractClaim(caseDetails);

        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_PAYMENT_CREATE_FAILURE_MESSAGE,
                    ccdCase.getExternalId(),
                    INITIATE_CLAIM_PAYMENT_CITIZEN
                ), exception
            );
        }
    }

    public Claim requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(userDetails.getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(MORE_TIME_REQUESTED_ONLINE.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = startUpdate(authorisation,
                eventRequestData, claim.getId(), false);
            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .responseDeadline(newResponseDeadline)
                .moreTimeRequested(true)
                .build();
            CCDCase ccdCase = caseMapper.to(updatedClaim);
            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(MORE_TIME_DEFENDANT_MSG)
                    .description(MORE_TIME_DEFENDANT_MSG)
                    .build())
                .data(ccdCase)
                .build();

            CaseDetails caseDetails = submitUpdate(
                authorisation,
                eventRequestData,
                caseDataContent,
                claim.getId(),
                false
            );

            return caseDetailsConverter.extractClaim(caseDetails);

        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_MORE_TIME_REQUESTED_ONLINE_FAILURE,
                    claim.getExternalId(),
                    MORE_TIME_REQUESTED_ONLINE.getValue()
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaim(startEventResponse);

            updatedClaim = updatedClaim.toBuilder()
                .claimDocumentCollection(claimDocumentCollection)
                .claimSubmissionOperationIndicators(
                    updateClaimSubmissionIndicatorByDocumentType(
                        updatedClaim.getClaimSubmissionOperationIndicators(),
                        claimDocumentType
                    )
                )
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
            );
            return caseDetailsConverter.extractClaim(caseDetails);
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

    private ClaimSubmissionOperationIndicators updateClaimSubmissionIndicatorByDocumentType(
        ClaimSubmissionOperationIndicators indicators,
        ClaimDocumentType documentType
    ) {
        ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder updatedIndicator
            = indicators.toBuilder();

        if (documentType == SEALED_CLAIM) {
            updatedIndicator.sealedClaimUpload(YES);
        } else if (documentType == CLAIM_ISSUE_RECEIPT) {
            updatedIndicator.claimIssueReceiptUpload(YES);
        }
        return updatedIndicator.build();
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
                isRepresented(userDetails)
            );

            LocalDateTime respondedAt = nowInUTC();
            LocalDate intentionToProceedDeadline = new StateTransitionCalculator(workingDayIndicator,
                intentionToProceedDeadlineDays).calculateDeadlineFromDate(respondedAt.toLocalDate());

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .response(response)
                .defendantEmail(defendantEmail)
                .respondedAt(respondedAt)
                .intentionToProceedDeadline(intentionToProceedDeadline)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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
                isRepresented(userDetails)
            );

            Claim existingClaim = toClaim(startEventResponse);
            Claim.ClaimBuilder claimBuilder = existingClaim.toBuilder();
            if ((pilotCourtService.isPilotCourt(getPreferredCourt(claimBuilder.build()), LA,
                existingClaim.getCreatedAt()) || pilotCourtService.isPilotCourt(getPreferredCourt(claimBuilder.build()),
                JDDO, existingClaim.getCreatedAt()))
            ) {
                claimBuilder.preferredDQPilotCourt(getPreferredCourt(claimBuilder.build()));
            }

            claimBuilder.claimantResponse(response)
                .claimantRespondedAt(nowInUTC())
                .dateReferredForDirections(nowInUTC())
                .preferredDQCourt(getPreferredCourt(claimBuilder.build()));

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, claimBuilder.build());

            return caseDetailsConverter.extractClaim(submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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

    private String getPreferredCourt(Claim existingClaim) {
        try {
            return directionsQuestionnaireService.getPreferredCourt(existingClaim);
        } catch (Exception e) {
            return null;
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
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .settlement(settlement)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .responseDeadline(newResponseDeadline)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            return submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
        return toClaim(startEventResponse).toBuilder();
    }

    private Claim toClaim(StartEventResponse startEventResponse) {
        return caseDetailsConverter.extractClaim(startEventResponse.getCaseDetails());
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Claim ccdClaim) {
        return caseDataContent(startEventResponse, caseMapper.to(ccdClaim));
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Object content) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .summary(CMC_CASE_UPDATE_SUMMARY)
                .description(SUBMITTING_CMC_CASE_UPDATE_DESCRIPTION)
                .build())
            .data(content)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails));
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

    public void saveDirectionsQuestionnaireDeadline(Long caseId, LocalDate dqDeadline, String authorisation) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(DIRECTIONS_QUESTIONNAIRE_DEADLINE, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .directionsQuestionnaireDeadline(dqDeadline)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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

    public Claim saveCaseEvent(String authorisation, Long caseId, CaseEvent caseEvent) {
        try {
            return sendCaseEvent(authorisation, caseEvent, caseId);
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
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
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .moneyReceivedOn(paidInFull.getMoneyReceivedOn())
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
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

    private boolean isRepresented(UserDetails userDetails) {
        return userDetails.isSolicitor() || userDetails.isCaseworker();
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
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .claimSubmissionOperationIndicators(indicators)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
            );

            return caseDetailsConverter.extractClaim(caseDetails);
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
                isRepresented(userDetails)
            );

            Claim claim = caseDetailsConverter.extractClaim(startEventResponse.getCaseDetails());

            Claim updatedClaim = claim.toBuilder()
                .letterHolderId(letterHolderId)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(
                anonymousCaseWorker.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
            );

            ccdCreateCaseService.grantAccessToCase(caseId.toString(), letterHolderId);
            Optional.ofNullable(claim.getLetterHolderId()).ifPresent(
                previousLetterHolderId ->
                    ccdCreateCaseService.removeAccessToCase(caseId.toString(), previousLetterHolderId)
            );

            return caseDetailsConverter.extractClaim(caseDetails);
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

    public Claim saveReviewOrder(Long caseId, ReviewOrder reviewOrder, String authorisation) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(ORDER_REVIEW_REQUESTED, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .reviewOrder(reviewOrder)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
            );
            return caseDetailsConverter.extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    ORDER_REVIEW_REQUESTED
                ), exception
            );
        }
    }

    public Claim saveCaseEventIOC(User user, Claim claim, CaseEvent caseEvent) {
        try {
            UserDetails userDetails = user.getUserDetails();

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                user.getAuthorisation(),
                eventRequestData,
                claim.getId(),
                isRepresented(userDetails)
            );

            CCDCase ccdCase = caseMapper.to(claim);

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, ccdCase);

            CaseDetails caseDetails = submitUpdate(user.getAuthorisation(),
                eventRequestData,
                caseDataContent,
                claim.getId(),
                isRepresented(userDetails)
            );
            return caseDetailsConverter.extractClaim(caseDetails);
        } catch (FeignException.UnprocessableEntity unprocessableEntity) {
            logger.warn("Event {} Ambiguous 422 from CCD, swallow this until fix for RDM-6411 is released", caseEvent);
            return claim;
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    claim.getId(),
                    caseEvent
                ), exception
            );
        }
    }

    public Claim addBulkPrintDetailsToClaim(
        String authorisation,
        List<BulkPrintDetails> bulkPrintDetails,
        CaseEvent caseEvent,
        Long caseId
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented(userDetails)
            );

            Claim updatedClaim = toClaimBuilder(startEventResponse)
                .bulkPrintDetails(bulkPrintDetails)
                .build();

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, updatedClaim);

            CaseDetails caseDetails = submitUpdate(authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented(userDetails)
            );
            return caseDetailsConverter.extractClaim(caseDetails);
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

    private Claim sendCaseEvent(String authorisation, CaseEvent caseEvent, Long caseId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

        StartEventResponse startEventResponse = startUpdate(
            authorisation,
            eventRequestData,
            caseId,
            isRepresented(userDetails)
        );

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(startEventResponse.getCaseDetails());
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, ccdCase);

        CaseDetails caseDetails = submitUpdate(authorisation,
            eventRequestData,
            caseDataContent,
            caseId,
            isRepresented(userDetails)
        );
        return caseDetailsConverter.extractClaim(caseDetails);
    }

    public Claim saveBreathingSpaceDetails(Claim claim, BreathingSpace breathingSpace, String authorisation) {

        CCDCase ccdCase = caseMapper.to(claim);
        CCDBreathingSpace ccdBreathingSpace = new CCDBreathingSpace(
            breathingSpace.getBsReferenceNumber(),
            CCDBreathingSpaceType.valueOf(breathingSpace.getBsType().name()),
            breathingSpace.getBsEnteredDate(),
            breathingSpace.getBsEnteredDateByInsolvencyTeam(),
            breathingSpace.getBsLiftedDate(),
            breathingSpace.getBsLiftedDateByInsolvencyTeam(),
            breathingSpace.getBsExpectedEndDate(),
            breathingSpace.getBsLiftedFlag(),
            null
        );

        ccdCase.setBreathingSpace(ccdBreathingSpace);
        if (breathingSpace.getBsLiftedFlag().equals("No")) {
            return caseDetailsConverter.extractClaim(update(authorisation, ccdCase, CaseEvent.BREATHING_SPACE_ENTERED));
        }
        return caseDetailsConverter.extractClaim(update(authorisation, ccdCase, CaseEvent.BREATHING_SPACE_LIFTED));
    }
}

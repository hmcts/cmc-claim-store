package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_BY_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_SEALED_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_ONLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_POST_PAYMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_PRE_PAYMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "ccd_enabled")
public class CoreCaseDataService {

    private final CaseMapper caseMapper;
    private final UserService userService;
    private final JsonMapper jsonMapper;
    private final ReferenceNumberService referenceNumberService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseAccessApi caseAccessApi;
    private final JobSchedulerService jobSchedulerService;

    @SuppressWarnings("squid:S00107") // All parameters are required here
    @Autowired
    public CoreCaseDataService(
        CaseMapper caseMapper,
        UserService userService,
        JsonMapper jsonMapper,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        JobSchedulerService jobSchedulerService
    ) {
        this.caseMapper = caseMapper;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.referenceNumberService = referenceNumberService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseAccessApi = caseAccessApi;
        this.jobSchedulerService = jobSchedulerService;
    }

    public CaseReference savePrePayment(String externalId, String authorisation) {
        UserDetails user = userService.getUserDetails(authorisation);

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("externalId", externalId);
            EventRequestData eventRequestData = eventRequest(SUBMIT_PRE_PAYMENT, user.getId());

            StartEventResponse startEventResponse = startCreate(authorisation, eventRequestData,
                user.isSolicitor() || user.isCaseworker());

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(
                    Event.builder()
                        .id(startEventResponse.getEventId())
                        .summary("CMC case submission event")
                        .description("Submitting CMC pre-payment case")
                        .build()
                ).data(data)
                .build();

            return new CaseReference(
                submitCreate(authorisation, eventRequestData, caseDataContent,
                    user.isSolicitor() || user.isCaseworker()).getId().toString()
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format("Failed storing claim in CCD store for claim %s", externalId), exception
            );
        }
    }

    public Claim submitPostPayment(String authorisation, Claim claim) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean isRepresented = userDetails.isSolicitor() || userDetails.isCaseworker();
        CCDCase ccdCase = caseMapper.to(claim);

        if (StringUtils.isBlank(claim.getReferenceNumber())) {
            ccdCase.setReferenceNumber(referenceNumberService.getReferenceNumber(isRepresented));
        }

        try {
            Long caseId = ccdCase.getId();
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(userDetails.getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(SUBMIT_POST_PAYMENT.getValue())
                .ignoreWarning(true)
                .build();

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build())
                .data(ccdCase)
                .build();

            CaseDetails caseDetails = submitUpdate(
                authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented
            );

            if (!isRepresented) {
                grantAccessToCase(caseDetails, claim.getLetterHolderId());
            }

            return extractClaim(caseDetails);

        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s",
                    ccdCase.getReferenceNumber(),
                    SUBMIT_POST_PAYMENT
                ), exception
            );
        }

    }

    public Claim requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        jobSchedulerService.rescheduleEmailNotificationsForDefendantResponse(claim, newResponseDeadline);

        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .responseDeadline(newResponseDeadline)
            .moreTimeRequested(true);

        return extractClaim(updateCaseDetails(authorisation, claim.getId(), MORE_TIME_REQUESTED_ONLINE, claimBuilder));

    }

    public CaseDetails saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        CaseEvent caseEvent = getCCJEvent(countyCourtJudgment.getCcjType());

        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .countyCourtJudgment(countyCourtJudgment)
            .countyCourtJudgmentRequestedAt(nowInUTC());

        return updateCaseDetails(authorisation, claim.getId(), caseEvent, claimBuilder);

    }

    private CaseEvent getCCJEvent(CountyCourtJudgmentType countyCourtJudgmentType) {
        if (countyCourtJudgmentType.equals(CountyCourtJudgmentType.ADMISSIONS)) {
            return CCJ_BY_ADMISSION;
        } else {
            return DEFAULT_CCJ_REQUESTED;
        }
    }

    public CaseDetails linkSealedClaimDocument(
        String authorisation,
        Long caseId,
        URI sealedClaimDocument
    ) {
        Claim.ClaimBuilder claimBuilder = Claim.builder()
            .sealedClaimDocument(sealedClaimDocument);

        return updateCaseDetails(authorisation, caseId, LINK_SEALED_CLAIM, claimBuilder);
    }

    public CaseDetails saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorisation
    ) {
        CaseEvent caseEvent = CaseEvent.valueOf(getResponseTypeName(response));

        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .response(response)
            .defendantEmail(defendantEmail)
            .respondedAt(nowInUTC());

        return updateCaseDetails(authorisation, claim.getId(), caseEvent, claimBuilder);
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
        Claim claim,
        ClaimantResponse response,
        String authorisation
    ) {
        CaseEvent caseEvent = CaseEvent.valueOf("CLAIMANT_RESPONSE_" + response.getType().name());

        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .claimantResponse(response)
            .claimantRespondedAt(nowInUTC());

        return extractClaim(updateCaseDetails(authorisation, claim.getId(), caseEvent, claimBuilder));
    }

    public CaseDetails saveSettlement(
        Claim claim,
        Settlement settlement,
        String authorisation,
        CaseEvent caseEvent
    ) {
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder.settlement(settlement);

        return updateCaseDetails(authorisation, claim.getId(), caseEvent, claimBuilder);
    }

    public CaseDetails reachSettlementAgreement(
        Claim claim,
        Settlement settlement,
        LocalDateTime settlementReachedAt,
        String authorisation,
        CaseEvent caseEvent
    ) {
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .settlement(settlement)
            .settlementReachedAt(settlementReachedAt);

        return updateCaseDetails(authorisation, claim.getId(), caseEvent, claimBuilder);
    }

    public CaseDetails updateResponseDeadline(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        addCaseDetails(claimBuilder, claim);

        claimBuilder
            .responseDeadline(newResponseDeadline);
        return updateCaseDetails(authorisation, claim.getId(), TEST_SUPPORT_UPDATE, claimBuilder);
    }

    public CaseDetails linkDefendant(
        String authorisation,
        Long caseId,
        String defendantId,
        String defendantEmail,
        CaseEvent caseEvent
    ) {
        Claim.ClaimBuilder claimBuilder = Claim.builder()
            .defendantEmail(defendantEmail)
            .defendantId(defendantId);

        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String userId = userDetails.getId();
            boolean isRepresented = userDetails.isSolicitor() || userDetails.isCaseworker();

            EventRequestData eventRequestData = eventRequest(caseEvent, userId);

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build())
                .data(caseMapper.to(updatedCase(startEventResponse.getCaseDetails().getData(), claimBuilder).build()))
                .build();

            return submitUpdate(authorisation, eventRequestData, caseDataContent, caseId, isRepresented);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    "Failed linking claim in CCD store for case id %s on event %s",
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    private CaseDetails updateCaseDetails(
        String authorisation,
        Long caseId,
        CaseEvent caseEvent,
        Claim.ClaimBuilder claimBuilder
    ) {
        try {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String userId = userDetails.getId();
            boolean isRepresented = userDetails.isSolicitor() || userDetails.isCaseworker();

            EventRequestData eventRequestData = eventRequest(caseEvent, userId);

            StartEventResponse startEventResponse = startUpdate(
                authorisation,
                eventRequestData,
                caseId,
                isRepresented
            );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build())
                .data(caseMapper.to(claimBuilder.build()))
                .build();

            return submitUpdate(authorisation, eventRequestData, caseDataContent, caseId, isRepresented);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    "Failed linking claim in CCD store for case id %s on event %s",
                    caseId,
                    caseEvent
                ), exception
            );
        }
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

    private Claim.ClaimBuilder updatedCase(Map<String, Object> data, Claim.ClaimBuilder claimBuilder) {
        CCDCase aCase = jsonMapper.fromMap(data, CCDCase.class);
        Claim claim = caseMapper.from(aCase);

        addCaseDetails(claimBuilder, claim);
        return claimBuilder;
    }

    private void addCaseDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        addClaimDetails(claimBuilder, claim);
        addClaimLinkDetails(claimBuilder, claim);
        addCCJDetails(claimBuilder, claim);
        addResponseDeadlineDetails(claimBuilder, claim);
        addClaimantResponseDetails(claimBuilder, claim);
        addReDeterminationDetails(claimBuilder, claim);
        addDefendantResponseDetails(claimBuilder, claim);
        addSettlementDetails(claimBuilder, claim);
        addStatesPaidDetails(claimBuilder, claim);
    }

    private void addStatesPaidDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claim.getMoneyReceivedOn().ifPresent(claimBuilder::moneyReceivedOn);
    }

    private void addSettlementDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claim.getSettlement().ifPresent(claimBuilder::settlement);
        Optional.ofNullable(claim.getSettlementReachedAt()).ifPresent(claimBuilder::settlementReachedAt);
    }

    private void addDefendantResponseDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claim.getResponse().ifPresent(claimBuilder::response);
        claimBuilder.respondedAt(claim.getRespondedAt());
    }

    private void addReDeterminationDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claim.getReDetermination().ifPresent(claimBuilder::reDetermination);
        claim.getReDeterminationRequestedAt().ifPresent(claimBuilder::reDeterminationRequestedAt);
    }

    private void addClaimantResponseDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claim.getClaimantResponse().ifPresent(claimBuilder::claimantResponse);
        claim.getClaimantRespondedAt().ifPresent(claimBuilder::claimantRespondedAt);
    }

    private void addResponseDeadlineDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        Optional.ofNullable(claim.getResponseDeadline()).ifPresent(claimBuilder::responseDeadline);
        if (claim.isMoreTimeRequested()) {
            claimBuilder.moreTimeRequested(true);
        }
    }

    private void addCCJDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        Optional.ofNullable(claim.getCountyCourtJudgment()).ifPresent(claimBuilder::countyCourtJudgment);
        Optional.ofNullable(claim.getCountyCourtJudgmentRequestedAt())
            .ifPresent(claimBuilder::countyCourtJudgmentRequestedAt);
    }

    private void addClaimLinkDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        Optional.ofNullable(claim.getDefendantEmail()).ifPresent(claimBuilder::defendantEmail);
        Optional.ofNullable(claim.getDefendantId()).ifPresent(claimBuilder::defendantId);
    }

    private void addClaimDetails(Claim.ClaimBuilder claimBuilder, Claim claim) {
        claimBuilder
            .id(claim.getId())
            .claimData(claim.getClaimData())
            .submitterId(claim.getSubmitterId())
            .issuedOn(claim.getIssuedOn())
            .externalId(claim.getExternalId())
            .submitterEmail(claim.getSubmitterEmail())
            .createdAt(claim.getCreatedAt())
            .letterHolderId(claim.getLetterHolderId())
            .features(claim.getFeatures())
            .referenceNumber(claim.getReferenceNumber());
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
                    .summary("CMC case update")
                    .description("Submitting CMC case update")
                    .build())
                .data(ccdCase)
                .build();

            return submitUpdate(authorisation, eventRequestData, caseDataContent, caseId,
                userDetails.isSolicitor() || userDetails.isCaseworker());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    "Failed updating claim in CCD store for claim %s on event %s",
                    ccdCase.getReferenceNumber(),
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

    private void grantAccessToCase(CaseDetails caseDetails, String letterHolderId) {
        User user = userService.authenticateAnonymousCaseWorker();
        caseAccessApi.grantAccessToCase(
            user.getAuthorisation(),
            authTokenGenerator.generate(),
            user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID,
            caseDetails.getId().toString(),
            new UserId(letterHolderId)
        );
    }

    private CaseDetails submitCreate(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        boolean represented
    ) {
        if (represented) {
            return coreCaseDataApi.submitForCaseworker(
                authorisation,
                this.authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }

        return coreCaseDataApi.submitForCitizen(
            authorisation,
            this.authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.isIgnoreWarning(),
            caseDataContent
        );
    }

    private StartEventResponse startCreate(
        String authorisation, EventRequestData eventRequestData, boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startForCaseworker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                eventRequestData.getEventId()
            );
        }

        return coreCaseDataApi.startForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData.getUserId(),
            eventRequestData.getJurisdictionId(),
            eventRequestData.getCaseTypeId(),
            eventRequestData.getEventId()
        );
    }

    private Claim extractClaim(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseDetails.getId());
        CCDCase ccdCase = jsonMapper.convertValue(caseData, CCDCase.class);

        return caseMapper.from(ccdCase);
    }

    public void saveDirectionsQuestionnaireDeadline(Long caseId, LocalDate dqDeadline, String authorisation) {
        Claim.ClaimBuilder claimBuilder = Claim.builder()
            .directionsQuestionnaireDeadline(dqDeadline);

        updateCaseDetails(authorisation, caseId, DIRECTIONS_QUESTIONNAIRE_DEADLINE, claimBuilder);
    }
}

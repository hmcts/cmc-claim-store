package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
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
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_ISSUED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_SEALED_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_ONLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_POST_PAYMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_PRE_PAYMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private final CaseMapper caseMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    private final ResponseMapper responseMapper;
    private final SettlementMapper settlementMapper;
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
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        ResponseMapper responseMapper,
        SettlementMapper settlementMapper,
        UserService userService,
        JsonMapper jsonMapper,
        ReferenceNumberService referenceNumberService,
        CoreCaseDataApi coreCaseDataApi,
        AuthTokenGenerator authTokenGenerator,
        CaseAccessApi caseAccessApi,
        JobSchedulerService jobSchedulerService
    ) {
        this.caseMapper = caseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.responseMapper = responseMapper;
        this.settlementMapper = settlementMapper;
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
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(user.getId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(SUBMIT_PRE_PAYMENT.getValue())
                .ignoreWarning(true)
                .build();

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
        ccdCase.setReferenceNumber(referenceNumberService.getReferenceNumber(isRepresented));

        CaseDetails caseDetails = update(authorisation, ccdCase, SUBMIT_POST_PAYMENT);

        if (!isRepresented) {
            grantAccessToCase(caseDetails, claim.getLetterHolderId());
        }

        return extractClaim(caseDetails);
    }

    public Claim requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        CCDCase ccdCase = caseMapper.to(claim);
        ccdCase.setResponseDeadline(newResponseDeadline);
        ccdCase.setMoreTimeRequested(YES);

        CaseDetails updates = update(authorisation, ccdCase, MORE_TIME_REQUESTED_ONLINE);
        jobSchedulerService.rescheduleEmailNotificationsForDefendantResponse(claim, newResponseDeadline);
        return extractClaim(updates);
    }

    public CaseDetails saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    ) {
        CCDCase ccdCase = caseMapper.to(claim);
        ccdCase.setCountyCourtJudgment(countyCourtJudgmentMapper.to(countyCourtJudgment));
        ccdCase.setCountyCourtJudgmentRequestedAt(nowInUTC());
        if (issue) {
            ccdCase.setCountyCourtJudgmentIssuedAt(nowInUTC());
            return update(authorisation, ccdCase, CCJ_ISSUED);
        } else {
            return update(authorisation, ccdCase, DEFAULT_CCJ_REQUESTED);
        }
    }

    public CaseDetails linkSealedClaimDocument(
        String authorisation,
        Long caseId,
        URI sealedClaimDocument
    ) {
        CCDCase ccdCase = CCDCase.builder()
            .id(caseId)
            .sealedClaimDocument(CCDDocument.builder().documentUrl(sealedClaimDocument.toString()).build())
            .build();
        return update(authorisation, ccdCase, LINK_SEALED_CLAIM);
    }

    public CaseDetails saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorisation
    ) {

        CCDCase ccdCase = CCDCase.builder()
            .id(claim.getId())
            .response(responseMapper.to(response))
            .defendantEmail(defendantEmail)
            .respondedAt(nowInUTC())
            .build();

        return update(authorisation, ccdCase, CaseEvent.valueOf(getResponseTypeName(response) + "_RESPONSE_SUBMITTED"));
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

    public CaseDetails saveSettlement(
        Long caseId,
        Settlement settlement,
        String authorisation,
        CaseEvent event
    ) {
        CCDCase ccdCase = CCDCase.builder()
            .id(caseId)
            .settlement(settlementMapper.to(settlement))
            .build();

        return update(authorisation, ccdCase, event);
    }

    public CaseDetails reachSettlementAgreement(
        Long caseId,
        Settlement settlement,
        String authorisation,
        CaseEvent event
    ) {
        CCDCase ccdCase = CCDCase.builder()
            .id(caseId)
            .settlement(settlementMapper.to(settlement))
            .settlementReachedAt(nowInUTC())
            .build();

        return update(authorisation, ccdCase, event);
    }

    public CaseDetails updateResponseDeadline(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        CCDCase ccdCase = CCDCase.builder()
            .id(claim.getId())
            .responseDeadline(newResponseDeadline)
            .build();

        return update(authorisation, ccdCase, TEST_SUPPORT_UPDATE);
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
}

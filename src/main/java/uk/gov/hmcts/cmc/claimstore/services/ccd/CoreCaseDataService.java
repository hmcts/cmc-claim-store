package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.ccd.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private final SaveCoreCaseDataService saveCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;
    private final CaseMapper caseMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    private final ResponseMapper responseMapper;
    private final SettlementMapper settlementMapper;
    private final UserService userService;
    private final JsonMapper jsonMapper;
    private final ReferenceNumberRepository referenceNumberRepository;

    @Autowired
    public CoreCaseDataService(
        SaveCoreCaseDataService saveCoreCaseDataService,
        UpdateCoreCaseDataService updateCoreCaseDataService,
        CaseMapper caseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        ResponseMapper responseMapper,
        SettlementMapper settlementMapper,
        UserService userService,
        JsonMapper jsonMapper,
        ReferenceNumberRepository referenceNumberRepository
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
        this.caseMapper = caseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.responseMapper = responseMapper;
        this.settlementMapper = settlementMapper;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.referenceNumberRepository = referenceNumberRepository;
    }

    public Claim save(String authorisation, Claim claim) {
        try {
            CCDCase ccdCase = caseMapper.to(claim);
            ccdCase.setReferenceNumber(this.getReferenceNumber(claim.getClaimData().isClaimantRepresented()));
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(claim.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(SUBMIT_CLAIM.getValue())
                .ignoreWarning(true)
                .build();

            CaseDetails caseDetails = saveCoreCaseDataService
                .save(
                    authorisation,
                    eventRequestData,
                    ccdCase,
                    claim.getClaimData().isClaimantRepresented(),
                    claim.getLetterHolderId()
                );

            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception);
        }
    }

    private String getReferenceNumber(Boolean claimantRepresented) {
        if (claimantRepresented) {
            return this.referenceNumberRepository.getReferenceNoForLegal();
        } else {
            return this.referenceNumberRepository.getReferenceNoForCitizen();
        }
    }

    public CaseDetails requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        CCDCase ccdCase = this.caseMapper.to(claim);
        ccdCase.setResponseDeadline(newResponseDeadline);
        ccdCase.setMoreTimeRequested(YES);
        return this.update(authorisation, ccdCase, MORE_TIME_REQUESTED);
    }

    public CaseDetails saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {

        CCDCase ccdCase = this.caseMapper.to(claim);
        ccdCase.setCountyCourtJudgment(countyCourtJudgmentMapper.to(countyCourtJudgment));
        ccdCase.setCountyCourtJudgmentRequestedAt(now());
        return this.update(authorisation, ccdCase, DEFAULT_CCJ_REQUESTED);
    }

    public CaseDetails saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorisation
    ) {

        CCDCase ccdCase = this.caseMapper.to(claim);
        ccdCase.setResponse(responseMapper.to((FullDefenceResponse) response));
        ccdCase.setDefendantEmail(defendantEmail);
        ccdCase.setRespondedAt(now());
        return this.update(authorisation, ccdCase, DEFENCE_SUBMITTED);
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

        return this.update(authorisation, ccdCase, event);
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
            .settlementReachedAt(now())
            .build();

        return this.update(authorisation, ccdCase, event);
    }

    public CaseDetails update(String authorisation, CCDCase ccdCase, CaseEvent caseEvent) {
        try {
            String userId = userService.getUserDetails(authorisation).getId();
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(userId)
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(caseEvent.getValue())
                .ignoreWarning(true)
                .build();

            return updateCoreCaseDataService
                .update(authorisation, eventRequestData, ccdCase, ccdCase.getId());
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed updating claim in CCD store for claim %s on event %s", ccdCase.getReferenceNumber(),
                    caseEvent), exception);
        }
    }

    private Claim extractClaim(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("id", caseDetails.getId());
        CCDCase ccdCase = convertToCCDCase(caseData);
        return caseMapper.from(ccdCase);
    }

    private CCDCase convertToCCDCase(Map<String, Object> mapData) {
        String json = jsonMapper.toJson(mapData);
        return jsonMapper.fromJson(json, CCDCase.class);
    }
}

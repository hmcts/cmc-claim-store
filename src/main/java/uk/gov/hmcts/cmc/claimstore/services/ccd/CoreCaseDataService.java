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
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

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
    private final ReferenceNumberService referenceNumberService;

    @SuppressWarnings("squid:S00107") // All parameters are required here
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
        ReferenceNumberService referenceNumberService
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
        this.caseMapper = caseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.responseMapper = responseMapper;
        this.settlementMapper = settlementMapper;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.referenceNumberService = referenceNumberService;
    }

    public Claim save(String authorisation, Claim claim) {
        boolean claimantRepresented = claim.getClaimData().isClaimantRepresented();
        String referenceNumber = referenceNumberService.getReferenceNumber(claimantRepresented);
        try {
            CCDCase ccdCase = caseMapper.to(claim);
            ccdCase.setReferenceNumber(referenceNumber);
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
                    claimantRepresented,
                    claim.getLetterHolderId()
                );

            return extractClaim(caseDetails);
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", referenceNumber), exception);
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
        ccdCase.setCountyCourtJudgmentRequestedAt(nowInUTC());
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
        ccdCase.setRespondedAt(nowInUTC());
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
            .settlementReachedAt(nowInUTC())
            .build();

        return this.update(authorisation, ccdCase, event);
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
        return this.update(authorisation, ccdCase, TEST_SUPPORT_UPDATE);
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
        CCDCase ccdCase = jsonMapper.convertValue(caseData, CCDCase.class);
        return caseMapper.from(ccdCase);
    }

}

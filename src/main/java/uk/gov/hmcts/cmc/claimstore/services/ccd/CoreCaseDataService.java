package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SUBMIT_CLAIM;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CoreCaseDataService {

    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";

    private final SaveCoreCaseDataService saveCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;
    private final CaseMapper caseMapper;
    private final CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    private final JsonMapper jsonMapper;


    @Autowired
    public CoreCaseDataService(
        SaveCoreCaseDataService saveCoreCaseDataService,
        UpdateCoreCaseDataService updateCoreCaseDataService,
        CaseMapper caseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper,
        JsonMapper jsonMapper
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
        this.caseMapper = caseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
        this.jsonMapper = jsonMapper;
    }

    public CaseDetails save(String authorisation, Claim claim) {
        try {
            CCDCase ccdCase = caseMapper.to(claim);
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(claim.getSubmitterId())
                .jurisdictionId(JURISDICTION_ID)
                .caseTypeId(CASE_TYPE_ID)
                .eventId(SUBMIT_CLAIM.getValue())
                .ignoreWarning(true)
                .build();

            return saveCoreCaseDataService
                .save(
                    authorisation,
                    eventRequestData,
                    jsonMapper.toJson(ccdCase),
                    claim.getClaimData().isClaimantRepresented()
                );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception);
        }
    }

    public CaseDetails saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {

        CCDCase ccdCase = this.caseMapper.to(claim);
        ccdCase.setCountyCourtJudgment(countyCourtJudgmentMapper.to(countyCourtJudgment));
        ccdCase.setCountyCourtJudgmentRequestedAt(now().format(ISO_DATE_TIME));
        return this.update(authorisation, ccdCase, DEFAULT_CCJ_REQUESTED);
    }

    public CaseDetails update(String authorisation, CCDCase ccdCase, CaseEvent caseEvent) {
        try {
            EventRequestData eventRequestData = EventRequestData.builder()
                .userId(ccdCase.getSubmitterId())
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
}

package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;

import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFAULT_CCJ_REQUESTED;
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

    @Autowired
    public CoreCaseDataService(
        SaveCoreCaseDataService saveCoreCaseDataService,
        UpdateCoreCaseDataService updateCoreCaseDataService,
        CaseMapper caseMapper,
        CountyCourtJudgmentMapper countyCourtJudgmentMapper
    ) {
        this.saveCoreCaseDataService = saveCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
        this.caseMapper = caseMapper;
        this.countyCourtJudgmentMapper = countyCourtJudgmentMapper;
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
                    ccdCase,
                    claim.getClaimData().isClaimantRepresented(),
                    claim.getLetterHolderId()
                );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(String
                .format("Failed storing claim in CCD store for claim %s", claim.getReferenceNumber()), exception);
        }
    }

    public CaseDetails requestMoreTimeForResponse(
        String authorisation,
        Claim claim,
        LocalDate newResponseDeadline
    ) {
        CCDCase ccdCase = this.caseMapper.to(claim);
        ccdCase.setResponseDeadline(newResponseDeadline);
        ccdCase.setMoreTimeRequested(true);
        return this.update(authorisation, ccdCase, MORE_TIME_REQUESTED);
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

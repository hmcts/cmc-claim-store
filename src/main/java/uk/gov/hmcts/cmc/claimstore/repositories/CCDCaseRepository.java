package uk.gov.hmcts.cmc.claimstore.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final CCDCaseApi ccdCaseApi;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CCDCaseRepository(
        CCDCaseApi ccdCaseApi,
        CoreCaseDataService coreCaseDataService
    ) {
        this.ccdCaseApi = ccdCaseApi;
        this.coreCaseDataService = coreCaseDataService;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return ccdCaseApi.getBySubmitterId(submitterId, authorisation);
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return ccdCaseApi.getByExternalId(externalId, authorisation);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return ccdCaseApi.getByReferenceNumber(claimReferenceNumber, authorisation);
    }

    @Override
    public void linkDefendant(String authorisation) {
        ccdCaseApi.linkDefendant(authorisation);
    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return ccdCaseApi.getByDefendantId(id, authorisation);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return ccdCaseApi.getByLetterHolderId(id, authorisation);
    }

    @Override
    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        coreCaseDataService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
    }

    @Override
    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorization) {
        coreCaseDataService.saveDefendantResponse(claim, defendantEmail, response, authorization);
    }

    @Override
    public void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline) {
        coreCaseDataService.requestMoreTimeForResponse(authorisation, claim, newResponseDeadline);
    }

    @Override
    public void updateSettlement(
        Claim claim,
        Settlement settlement,
        String authorisation,
        String userAction) {
        coreCaseDataService.saveSettlement(claim.getId(), settlement, authorisation, CaseEvent.valueOf(userAction));
    }

    @Override
    public void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, String userAction) {
        coreCaseDataService.reachSettlementAgreement(claim.getId(), settlement, authorisation,
            CaseEvent.valueOf(userAction));
    }

    @Override
    public Claim saveClaim(String authorisation, Claim claim) {
        return coreCaseDataService.save(authorisation, claim);
    }

}

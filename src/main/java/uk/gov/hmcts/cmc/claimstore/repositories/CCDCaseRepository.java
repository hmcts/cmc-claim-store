package uk.gov.hmcts.cmc.claimstore.repositories;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.Redetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.net.URI;
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
    public Long getOnHoldIdByExternalId(String externalId, String authorisation) {
        return ccdCaseApi.getOnHoldIdByExternalId(externalId, authorisation);
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
    public List<Claim> getByClaimantEmail(String email, String authorisation) {
        return ccdCaseApi.getBySubmitterEmail(email, authorisation);
    }

    @Override
    public List<Claim> getByDefendantEmail(String email, String authorisation) {
        return ccdCaseApi.getByDefendantEmail(email, authorisation);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return ccdCaseApi.getByLetterHolderId(id, authorisation);
    }

    @Override
    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    ) {
        coreCaseDataService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment, issue);

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
    public void saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization) {
        throw new NotImplementedException("Save claimant response not implemented on CCD");
    }

    @Override
    public void paidInFull(Claim claim, PaidInFull paidInFull, String authorisation) {
        throw new NotImplementedException("Save received to be implemented on CCD");
    }

    @Override
    public void updateDirectionsQuestionnaireDeadline(String externalId, LocalDate dqDeadline, String authorization) {
        throw new NotImplementedException("We do not implement CCD yet");
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
    public CaseReference savePrePaymentClaim(String externalId, String authorisation) {
        try {
            return new CaseReference(getOnHoldIdByExternalId(externalId, authorisation).toString());
        } catch (NotFoundException e) {
            return coreCaseDataService.savePrePayment(externalId, authorisation);
        }
    }

    @Override
    public Claim saveClaim(String authorisation, Claim claim) {
        return coreCaseDataService.submitPostPayment(authorisation, claim);
    }

    @Override
    public void linkSealedClaimDocument(String authorisation, Claim claim, URI documentURI) {
        coreCaseDataService.linkSealedClaimDocument(authorisation, claim.getId(), documentURI);
    }

    @Override
    public void saveRedetermination(String authorisation, Claim claim, Redetermination redetermination, String submitterId) {
        throw new NotImplementedException("We do not implement CCD yet");
    }
}

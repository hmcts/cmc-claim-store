package uk.gov.hmcts.cmc.claimstore.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final CCDCaseApi ccdCaseApi;
    private final CoreCaseDataService coreCaseDataService;
    private final UserService userService;

    @Autowired
    public CCDCaseRepository(
        CCDCaseApi ccdCaseApi,
        CoreCaseDataService coreCaseDataService,
        UserService userService
    ) {
        this.ccdCaseApi = ccdCaseApi;
        this.coreCaseDataService = coreCaseDataService;
        this.userService = userService;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return ccdCaseApi.getBySubmitterId(submitterId, authorisation);
    }

    @Override
    @LogExecutionTime
    public Optional<Claim> getClaimByExternalId(String externalId, User user) {
        return ccdCaseApi.getByExternalId(externalId, user);
    }

    @LogExecutionTime
    public Optional<Claim> getClaimByExternalId(String externalId, String authorization) {
        return ccdCaseApi.getByExternalId(externalId, userService.getUser(authorization));
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
    public List<Claim> getByPaymentReference(String payReference, String authorisation) {
        return ccdCaseApi.getByPaymentReference(payReference, authorisation);
    }

    @Override
    public List<Claim> getClaimsByState(ClaimState claimState, User user) {
        return ccdCaseApi.getClaimsByState(claimState, user);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return ccdCaseApi.getByLetterHolderId(id, authorisation);
    }

    @Override
    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        coreCaseDataService.saveCountyCourtJudgment(authorisation, claim.getId(), countyCourtJudgment);
    }

    @Override
    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        LocalDate claimantResponseDeadline,
        String authorization
    ) {
        coreCaseDataService.saveDefendantResponse(claim.getId(), defendantEmail, response, authorization);
    }

    @Override
    public Claim saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization) {
        return coreCaseDataService.saveClaimantResponse(claim.getId(), response, authorization);
    }

    @Override
    public void paidInFull(Claim claim, PaidInFull paidInFull, String authorisation) {
        coreCaseDataService.savePaidInFull(claim.getId(), paidInFull, authorisation);
    }

    @Override
    public void updateDirectionsQuestionnaireDeadline(Claim claim, LocalDate dqDeadline, String authorization) {
        coreCaseDataService.saveDirectionsQuestionnaireDeadline(claim.getId(), dqDeadline, authorization);
    }

    @Override
    public void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline) {
        coreCaseDataService.requestMoreTimeForResponse(authorisation, claim, newResponseDeadline);
    }

    @Override
    public Claim initiatePayment(User user, Claim claim) {
        return coreCaseDataService.initiatePaymentForCitizenCase(user, claim);
    }

    @Override
    public void updateSettlement(
        Claim claim,
        Settlement settlement,
        String authorisation,
        CaseEvent caseEvent
    ) {
        coreCaseDataService.saveSettlement(claim.getId(), settlement, authorisation, caseEvent);
    }

    @Override
    public void reachSettlementAgreement(
        Claim claim,
        Settlement settlement,
        String authorisation,
        CaseEvent caseEvent
    ) {
        coreCaseDataService.reachSettlementAgreement(claim.getId(), settlement, nowInUTC(), authorisation,
            caseEvent);
    }

    @Override
    public Claim saveClaim(User user, Claim claim) {
        return coreCaseDataService.createNewCase(user, claim);
    }

    @Override
    @LogExecutionTime
    public Claim saveRepresentedClaim(User user, Claim claim) {
        return coreCaseDataService.createRepresentedClaim(user, claim);
    }

    @Override
    public Claim saveClaimDocuments(
        String authorisation,
        Long claimId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        return coreCaseDataService
            .saveClaimDocuments(authorisation, claimId, claimDocumentCollection, claimDocumentType);
    }

    @Override
    public Claim linkLetterHolder(Long claimId, String letterHolderId) {
        return coreCaseDataService.linkLetterHolder(claimId, letterHolderId);
    }

    @Override
    public Claim saveReviewOrder(Long caseId, ReviewOrder reviewOrder, String authorisation) {
        return coreCaseDataService.saveReviewOrder(caseId, reviewOrder, authorisation);
    }

    @Override
    public Claim updateClaimSubmissionOperationStatus(String authorisation, Long claimId,
                                                      ClaimSubmissionOperationIndicators indicators,
                                                      CaseEvent caseEvent) {
        return
            coreCaseDataService.saveClaimSubmissionOperationIndicators(claimId, indicators, authorisation, caseEvent);
    }

    @Override
    public void saveReDetermination(
        String authorisation,
        Claim claim,
        ReDetermination reDetermination
    ) {
        CaseEvent event = reDetermination.getPartyType() == MadeBy.DEFENDANT
            ? REFER_TO_JUDGE_BY_DEFENDANT
            : CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;

        coreCaseDataService.saveReDetermination(authorisation, claim.getId(), reDetermination, event);
    }

    @Override
    public Claim saveCaseEvent(String authorisation, Claim claim, CaseEvent caseEvent) {
        return coreCaseDataService.saveCaseEvent(authorisation, claim.getId(), caseEvent);
    }

    @Override
    public void updateClaimState(String authorisation, Long claimId, ClaimState state) {
        if (state == ClaimState.OPEN) {
            coreCaseDataService.saveCaseEvent(authorisation, claimId, CaseEvent.ISSUE_CASE);
        } else {
            throw new UnsupportedOperationException("State transition not allowed for " + state.name());
        }
    }
}

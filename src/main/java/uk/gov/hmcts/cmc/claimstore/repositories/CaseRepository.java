package uk.gov.hmcts.cmc.claimstore.repositories;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
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
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, User user);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    );

    void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        LocalDate claimantResponseDeadline,
        String authorization
    );

    Claim saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization);

    void paidInFull(Claim claim, PaidInFull paidInFull, String authorisation);

    void updateDirectionsQuestionnaireDeadline(Claim claim, LocalDate dqDeadline, String authorization);

    void linkDefendant(String authorisation);

    List<Claim> getByDefendantId(String id, String authorisation);

    List<Claim> getByClaimantEmail(String email, String authorisation);

    List<Claim> getByDefendantEmail(String email, String authorisation);

    List<Claim> getByPaymentReference(String payReference, String authorisation);

    List<Claim> getClaimsByState(ClaimState claimState, User user);

    Optional<Claim> getByLetterHolderId(String id, String authorisation);

    void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline);

    void updateSettlement(Claim claim, Settlement settlement, String authorisation, CaseEvent caseEvent);

    void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, CaseEvent caseEvent);

    Claim saveClaim(User user, Claim claim);

    Claim saveRepresentedClaim(User user, Claim claim);

    void saveReDetermination(String authorisation, Claim claim, ReDetermination reDetermination);

    Claim saveCaseEvent(String authorisation, Claim claim, CaseEvent caseEvent);

    Claim initiatePayment(User user, Claim claim);

    Claim saveCaseEventIOC(User user, Claim claim, CaseEvent caseEvent);

    Claim saveClaimDocuments(
        String authorisation,
        Long claimId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    );

    Claim updateClaimSubmissionOperationStatus(
        String authorisation,
        Long claimId,
        ClaimSubmissionOperationIndicators indicators,
        CaseEvent caseEvent);

    void updateClaimState(String authorisation, Long claimId, ClaimState state);

    Claim linkLetterHolder(Long claimId, String letterHolderId);

    Claim saveReviewOrder(Long caseId, ReviewOrder reviewOrder, String authorisation);

}


package uk.gov.hmcts.cmc.claimstore.repositories;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, String authorisation);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    );

    void saveDefendantResponse(Claim claim, String defendantEmail, Response response, String authorization);

    Claim saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization);

    void paidInFull(Claim claim, PaidInFull paidInFull, String authorisation);

    void updateDirectionsQuestionnaireDeadline(Claim claim, LocalDate dqDeadline, String authorization);

    void linkDefendant(String authorisation);

    List<Claim> getByDefendantId(String id, String authorisation);

    List<Claim> getByClaimantEmail(String email, String authorisation);

    List<Claim> getByDefendantEmail(String email, String authorisation);

    List<Claim> getByPaymentReference(String payReference, String authorisation);

    Optional<Claim> getByLetterHolderId(String id, String authorisation);

    void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline);

    void updateSettlement(Claim claim, Settlement settlement, String authorisation, CaseEvent caseEvent);

    void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, CaseEvent caseEvent);

    CaseReference savePrePaymentClaim(String externalId, String authorisation);

    Claim saveClaim(String authorisation, Claim claim);

    void saveReDetermination(String authorisation, Claim claim, ReDetermination reDetermination);

    void saveCaseEvent(String authorisation, Claim claim, CaseEvent caseEvent);

    Claim saveClaimDocuments(String authorisation, Long claimId, ClaimDocumentCollection claimDocumentCollection);
}


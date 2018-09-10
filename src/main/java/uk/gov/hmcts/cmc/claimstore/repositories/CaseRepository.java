package uk.gov.hmcts.cmc.claimstore.repositories;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, String authorisation);

    Long getOnHoldIdByExternalId(String externalId, String authorisation);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment,
        boolean issue
    );

    void saveDefendantResponse(Claim claim, String defendantEmail, Response response, String authorization);

    void saveClaimantResponse(Claim claim, ClaimantResponse response, String authorization);

    void linkDefendant(String authorisation);

    List<Claim> getByDefendantId(String id, String authorisation);

    List<Claim> getByClaimantEmail(String email, String authorisation);

    List<Claim> getByDefendantEmail(String email, String authorisation);

    Optional<Claim> getByLetterHolderId(String id, String authorisation);

    void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline);

    void updateSettlement(Claim claim, Settlement settlement, String authorisation, String userAction);

    void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, String userAction);

    CaseReference savePrePaymentClaim(String externalId, String authorisation);

    Claim saveClaim(String authorisation, Claim claim);

    void linkSealedClaimDocument(String authorisation, Claim claim, URI documentURI);
}


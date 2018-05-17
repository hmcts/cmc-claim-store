package uk.gov.hmcts.cmc.claimstore.repositories;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, String authorisation);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment);

    void saveDefendantResponse(Claim claim, String defendantEmail, Response response, String authorization);

    void linkDefendant(String authorisation);

    List<Claim> getByDefendantId(String id, String authorisation);

    Optional<Claim> getByLetterHolderId(String id, String authorisation);

    void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline);

    void updateSettlement(Claim claim, Settlement settlement, String authorisation, String userAction);

    void reachSettlementAgreement(Claim claim, Settlement settlement, String authorisation, String userAction);

    Claim saveClaim(String authorisation, Claim claim);
}


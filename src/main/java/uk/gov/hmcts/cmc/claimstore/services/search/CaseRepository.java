package uk.gov.hmcts.cmc.claimstore.services.search;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, String authorisation);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment);

    void saveDefendantResponse(Claim claim, String defendantId, String defendantEmail,
                               Response response, String authorization);

    Claim linkDefendantV1(String externalId, String defendantId, String authorisation);

    void linkDefendantV2(String authorisation);

    List<Claim> getByDefendantId(String id, String authorisation);

    Optional<Claim> getByLetterHolderId(String id, String authorisation);

    void requestMoreTimeForResponse(String authorisation, Claim claim, LocalDate newResponseDeadline);

    void updateSettlement(Claim claim, Settlement settlement,
                          String authorisation, String event, LocalDateTime settlementReachedAt);
}


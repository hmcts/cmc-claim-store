package uk.gov.hmcts.cmc.claimstore.services.search;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getByExternalId(String externalId, String authorisation);

    Optional<Claim> getByReferenceNumber(String claimReferenceNumber, String authorisation);

    void linkDefendant(String externalId, String defendantId, String authorisation);
}

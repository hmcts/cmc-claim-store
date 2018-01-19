package uk.gov.hmcts.cmc.claimstore.services.search;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

public interface ClaimSearchService {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getClaimByExternalId(String externalId, String authorisation);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);
}

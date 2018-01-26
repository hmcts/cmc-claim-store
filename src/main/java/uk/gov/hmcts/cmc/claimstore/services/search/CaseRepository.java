package uk.gov.hmcts.cmc.claimstore.services.search;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CaseRepository {
    List<Claim> getBySubmitterId(String submitterId, String authorisation);

    Optional<Claim> getByExternalId(String externalId, String authorisation);

    Optional<Claim> getByReferenceNumber(String claimReferenceNumber, String authorisation);

    void linkDefendant(String externalId, String defendantId, String authorisation);

    Long saveSubmittedByClaimant(
        String claim,
        String submitterId,
        String letterHolderId,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        String externalId,
        String submitterEmail
    );

    Long saveRepresented(
        String claim,
        String submitterId,
        LocalDate issuedOn,
        LocalDate responseDeadline,
        String externalId,
        String submitterEmail
    );
}

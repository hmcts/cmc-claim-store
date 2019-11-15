package uk.gov.hmcts.cmc.claimstore.repositories.support;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@ConditionalOnProperty("claim-store.test-support.enabled")
public interface SupportRepository {

    void updateResponseDeadline(String authorisation, Claim claim, LocalDate responseDeadline);

    Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation);

    void linkDefendantToClaim(Claim claim, String defendantId, String defendantEmail);
}

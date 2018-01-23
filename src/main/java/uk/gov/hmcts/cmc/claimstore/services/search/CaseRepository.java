package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseDBI;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service
public class CaseRepository {

    private final CaseDBI caseDBI;

    public CaseRepository(CaseDBI caseDBI) {
        this.caseDBI = caseDBI;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return caseDBI.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return caseDBI.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return caseDBI.getByClaimReferenceNumber(claimReferenceNumber);
    }
}

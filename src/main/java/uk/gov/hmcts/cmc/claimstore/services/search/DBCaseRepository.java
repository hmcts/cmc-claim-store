package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimSearchRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimSearchRepository claimSearchRepository;

    public DBCaseRepository(ClaimSearchRepository claimSearchRepository) {
        this.claimSearchRepository = claimSearchRepository;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimSearchRepository.getBySubmitterId(submitterId);
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return claimSearchRepository.getClaimByExternalId(externalId);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return claimSearchRepository.getByClaimReferenceNumber(claimReferenceNumber);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseDBI;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service("claimSearchService")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final CaseDBI caseDBI;

    public DBCaseRepository(CaseDBI caseDBI) {
        this.caseDBI = caseDBI;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return caseDBI.getBySubmitterId(submitterId);
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return caseDBI.getClaimByExternalId(externalId);
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        return caseDBI.getByClaimReferenceNumber(claimReferenceNumber);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimSearchRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service
public class DBCaseRepository implements CaseRepository {

    private final ClaimSearchRepository claimSearchRepository;
    private final UserService userService;

    public DBCaseRepository(
        ClaimSearchRepository claimSearchRepository,
        UserService userService
    ) {
        this.claimSearchRepository = claimSearchRepository;
        this.userService = userService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimSearchRepository.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return claimSearchRepository.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return claimSearchRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final ClaimRepository claimRepository;
    private final UserService userService;

    public DBCaseRepository(
        ClaimRepository claimRepository,
        UserService userService
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return claimRepository.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }

    @Override
    public Optional<Claim> linkDefendant(String externalId, String defendantId, String authorisation) {
        Optional<Claim> claim = claimRepository.getClaimByExternalId(externalId);
        if (claim.isPresent()) {
            claimRepository.linkDefendant(claim.orElseThrow(IllegalStateException::new).getId(), defendantId);
            claim = claimRepository.getClaimByExternalId(externalId);
        }
        return claim;

    }

    @Override
    public List<Claim> getByDefendantId(String id, String authorisation) {
        return claimRepository.getByDefendantId(id);
    }

    @Override
    public Optional<Claim> getByLetterHolderId(String id, String authorisation) {
        return claimRepository.getByLetterHolderId(id);
    }
}

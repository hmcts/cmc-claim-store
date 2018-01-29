package uk.gov.hmcts.cmc.claimstore.services.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseDBI;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url", havingValue = "false")
public class DBCaseRepository implements CaseRepository {

    private final CaseDBI caseDBI;
    private final UserService userService;

    public DBCaseRepository(
        CaseDBI caseDBI,
        UserService userService
    ) {
        this.caseDBI = caseDBI;
        this.userService = userService;
    }

    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        return caseDBI.getBySubmitterId(submitterId);
    }

    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        return caseDBI.getClaimByExternalId(externalId);
    }

    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return caseDBI.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);
    }

    @Override
    public Optional<Claim> linkDefendant(String externalId, String defendantId, String authorisation) {
        Optional<Claim> claim = caseDBI.getClaimByExternalId(externalId);
        if (claim.isPresent()) {
            caseDBI.linkDefendant(claim.orElseThrow(IllegalStateException::new).getId(), defendantId);
            claim = caseDBI.getClaimByExternalId(externalId);
        }
        return claim;

    }
}

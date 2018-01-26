package uk.gov.hmcts.cmc.claimstore.services.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimSearchRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimSearchRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final Logger logger = LoggerFactory.getLogger(CCDCaseRepository.class);

    private final ClaimSearchRepository claimSearchRepository;
    private final CCDClaimSearchRepository ccdClaimSearchRepository;
    private final UserService userService;

    public CCDCaseRepository(
        ClaimSearchRepository claimSearchRepository,
        CCDClaimSearchRepository ccdClaimSearchRepository,
        UserService userService
    ) {
        this.claimSearchRepository = claimSearchRepository;
        this.ccdClaimSearchRepository = ccdClaimSearchRepository;
        this.userService = userService;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        List<Claim> dbClaims = claimSearchRepository.getBySubmitterId(submitterId);
        List<Claim> ccdClaims = ccdClaimSearchRepository.getBySubmitterId(submitterId, authorisation);
        logClaimDetails(dbClaims, ccdClaims);
        return dbClaims;
    }

    private void logClaimDetails(List<Claim> dbClaims, List<Claim> ccdClaims) {
        if (!dbClaims.isEmpty() && !ccdClaims.isEmpty()) {
            dbClaims.forEach(claim -> logger.info(format("claim with reference number %s for user %s exist in ccd",
                claim.getReferenceNumber(), claim.getSubmitterId())));
        }
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        Optional<Claim> claim = claimSearchRepository.getClaimByExternalId(externalId);
        Optional<Claim> ccdClaim = ccdClaimSearchRepository.getByExternalId(externalId, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with external id %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();

        Optional<Claim> claim
            = claimSearchRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);

        Optional<Claim> ccdClaim = ccdClaimSearchRepository.getByReferenceNumber(claimReferenceNumber, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with reference number %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }
}

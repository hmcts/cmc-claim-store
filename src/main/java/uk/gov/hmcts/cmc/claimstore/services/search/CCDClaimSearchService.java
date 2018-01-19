package uk.gov.hmcts.cmc.claimstore.services.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimSearchRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service("claimSearchService")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDClaimSearchService implements ClaimSearchService {
    private final Logger logger = LoggerFactory.getLogger(CCDClaimSearchService.class);

    private final ClaimSearchRepository claimSearchRepository;
    private final CCDClaimRepository ccdClaimSearchRepository;

    public CCDClaimSearchService(
        ClaimSearchRepository claimSearchRepository,
        CCDClaimRepository ccdClaimSearchRepository
    ) {

        this.claimSearchRepository = claimSearchRepository;
        this.ccdClaimSearchRepository = ccdClaimSearchRepository;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        final List<Claim> postgresClaims = claimSearchRepository.getBySubmitterId(submitterId);
        final List<Claim> ccdClaims = ccdClaimSearchRepository.getBySubmitterId(submitterId, authorisation);
        compareAndLog(postgresClaims, ccdClaims);
        return postgresClaims;
    }

    private void compareAndLog(List<Claim> postgresClaims, List<Claim> ccdClaims) {
        if (!postgresClaims.isEmpty() && !ccdClaims.isEmpty()) {
            postgresClaims.forEach(postgressClaim -> {
                ccdClaims.stream()
                    .filter(c -> c.getReferenceNumber().equals(postgressClaim.getReferenceNumber()))
                    .findFirst()
                    .ifPresent((c) -> logger.info("claim with reference number %s exist in ccd",
                        postgressClaim.getReferenceNumber()));
            });
        }
    }

    @Override
    public Optional<Claim> getClaimByExternalId(String externalId, String authorisation) {
        final Optional<Claim> claim = claimSearchRepository.getClaimByExternalId(externalId);
        final Optional<Claim> ccdClaim = ccdClaimSearchRepository.getByClaimExternalId(externalId, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            if (claim.get().equals(ccdClaim.get())) {
                logger.info("claim with reference number %s exist in ccd", claim.get().getReferenceNumber());
            }
        }
        return claim;
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        final Optional<Claim> claim = claimSearchRepository.getByClaimReferenceNumber(claimReferenceNumber);

        final Optional<Claim> ccdClaim
            = ccdClaimSearchRepository.getByClaimReferenceNumber(claimReferenceNumber, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            if (claim.get().equals(ccdClaim.get())) {
                logger.info("claim with reference number %s exist in ccd", claim.get().getReferenceNumber());
            }
        }
        return claim;
    }
}

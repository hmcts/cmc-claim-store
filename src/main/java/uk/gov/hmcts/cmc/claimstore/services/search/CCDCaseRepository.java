package uk.gov.hmcts.cmc.claimstore.services.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseDBI;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service("claimSearchService")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final Logger logger = LoggerFactory.getLogger(CCDCaseRepository.class);

    private final CaseDBI caseDBI;
    private final CCDClaimRepository ccdClaimSearchRepository;

    public CCDCaseRepository(
        CaseDBI caseDBI,
        CCDClaimRepository ccdClaimSearchRepository
    ) {
        this.caseDBI = caseDBI;
        this.ccdClaimSearchRepository = ccdClaimSearchRepository;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        final List<Claim> dbClaims = caseDBI.getBySubmitterId(submitterId);
        final List<Claim> ccdClaims = ccdClaimSearchRepository.getBySubmitterId(submitterId, authorisation);
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
        final Optional<Claim> claim = caseDBI.getClaimByExternalId(externalId);
        final Optional<Claim> ccdClaim = ccdClaimSearchRepository.getByClaimExternalId(externalId, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with external id %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        final Optional<Claim> claim = caseDBI.getByClaimReferenceNumber(claimReferenceNumber);

        final Optional<Claim> ccdClaim
            = ccdClaimSearchRepository.getByClaimReferenceNumber(claimReferenceNumber, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with reference number %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }
}

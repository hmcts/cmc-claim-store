package uk.gov.hmcts.cmc.claimstore.services.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.LegacyClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final Logger logger = LoggerFactory.getLogger(CCDCaseRepository.class);

    private final LegacyClaimRepository legacyClaimRepository;
    private final CCDCaseRepository ccdCaseRepository;
    private final UserService userService;

    public CCDCaseRepository(
        LegacyClaimRepository legacyClaimRepository,
        CCDCaseRepository ccdCaseRepository,
        UserService userService
    ) {
        this.legacyClaimRepository = legacyClaimRepository;
        this.ccdCaseRepository = ccdCaseRepository;
        this.userService = userService;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        final List<Claim> dbClaims = legacyClaimRepository.getBySubmitterId(submitterId);
        final List<Claim> ccdClaims = ccdCaseRepository.getBySubmitterId(submitterId, authorisation);
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
    public Optional<Claim> getByExternalId(String externalId, String authorisation) {
        final Optional<Claim> claim = legacyClaimRepository.getByExternalId(externalId);
        final Optional<Claim> ccdClaim = ccdCaseRepository.getByExternalId(externalId, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with external id %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }

    @Override
    public Optional<Claim> getByReferenceNumber(String claimReferenceNumber, String authorisation) {
        final String submitterId = userService.getUserDetails(authorisation).getId();
        final Optional<Claim> claim
            = legacyClaimRepository.getByReferenceAndSubmitter(claimReferenceNumber, submitterId);

        final Optional<Claim> ccdClaim
            = ccdCaseRepository.getByReferenceNumber(claimReferenceNumber, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with reference number %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }

    @Override
    public void linkDefendant(String externalId, String defendantId, String authorisation) {
        Claim claim = legacyClaimRepository.getByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException("Claim not found by externalId: " + externalId));
        legacyClaimRepository.linkDefendant(claim.getId(), defendantId);
    }
}

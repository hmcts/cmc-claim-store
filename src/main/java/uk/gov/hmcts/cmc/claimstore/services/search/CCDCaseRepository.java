package uk.gov.hmcts.cmc.claimstore.services.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimSearchRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.LegacyCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service("caseRepository")
@ConditionalOnProperty(prefix = "core_case_data", name = "api.url")
public class CCDCaseRepository implements CaseRepository {
    private final Logger logger = LoggerFactory.getLogger(CCDCaseRepository.class);

    private final LegacyCaseRepository legacyCaseRepository;
    private final CCDClaimSearchRepository ccdClaimSearchRepository;
    private final CoreCaseDataService coreCaseDataService;
    private final UserService userService;
    private final JsonMapper jsonMapper;

    public CCDCaseRepository(
        LegacyCaseRepository legacyCaseRepository,
        CCDClaimSearchRepository ccdClaimSearchRepository,
        CoreCaseDataService coreCaseDataService,
        UserService userService,
        JsonMapper jsonMapper
    ) {
        this.legacyCaseRepository = legacyCaseRepository;
        this.ccdClaimSearchRepository = ccdClaimSearchRepository;
        this.coreCaseDataService = coreCaseDataService;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public List<Claim> getBySubmitterId(String submitterId, String authorisation) {
        List<Claim> dbClaims = legacyCaseRepository.getBySubmitterId(submitterId);
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
        Optional<Claim> claim = legacyCaseRepository.getClaimByExternalId(externalId);
        Optional<Claim> ccdClaim = ccdClaimSearchRepository.getByExternalId(externalId, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with external id %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
            // temporary work around to pass CCD id. must be removed
            return Optional.of(SampleClaim.builder().build(claim.get(), ccdClaim.get().getId()));
        }

        return claim;
    }

    @Override
    public Optional<Claim> getByClaimReferenceNumber(String claimReferenceNumber, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        Optional<Claim> claim
            = legacyCaseRepository.getByClaimReferenceAndSubmitter(claimReferenceNumber, submitterId);

        Optional<Claim> ccdClaim
            = ccdClaimSearchRepository.getByReferenceNumber(claimReferenceNumber, authorisation);

        if (claim.isPresent() && ccdClaim.isPresent()) {
            logger.info(format("claim with reference number %s user %s exist in ccd",
                claim.get().getReferenceNumber(), claim.get().getSubmitterId()));
        }

        return claim;
    }

    @Override
    public Optional<Claim> linkDefendant(String externalId, String defendantId, String authorisation) {
        Optional<Claim> claim = legacyCaseRepository.getClaimByExternalId(externalId);
        if (claim.isPresent()) {
            legacyCaseRepository.linkDefendant(claim.orElseThrow(IllegalStateException::new).getId(), defendantId);
            claim = legacyCaseRepository.getClaimByExternalId(externalId);
        }
        return claim;
    }

    @Override
    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        legacyCaseRepository.saveCountyCourtJudgment(claim.getExternalId(), jsonMapper.toJson(countyCourtJudgment));
        coreCaseDataService.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
    }
}

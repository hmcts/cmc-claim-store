package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;

@Service
public class ClaimMigrator {

    private static final Logger logger = LoggerFactory.getLogger(ClaimMigrator.class);

    private ClaimRepository claimRepository;
    private UserService userService;
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    public ClaimMigrator(
        ClaimRepository claimRepository,
        UserService userService,
        CoreCaseDataService coreCaseDataService) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void migrate() {
        logger.info("===== MIGRATE CLAIMS TO CCD =====");

        String authorisation = userService.authenticateSystemUpdateUser();
        List<Claim> notMigratedClaims = claimRepository.getAllNotMigratedClaims();

        logger.info("User token: " + authorisation);

        logger.info("\t Claims to migrate: " + notMigratedClaims.size());

        notMigratedClaims.forEach(claim -> {
            logger.info("\t\t start migrating claim: " + claim.getReferenceNumber());

            Optional<Claim> ccdClaim = coreCaseDataService.retrieve(authorisation, claim.getReferenceNumber());
            if (ccdClaim.isPresent()) {
                logger.info("\t\t claim exists - overwrite");
                coreCaseDataService.overwrite(authorisation, ccdClaim.get());
            } else {
                logger.info("\t\t claim created in ccd");
                coreCaseDataService.create(authorisation, claim);
            }

            claimRepository.markAsMigrated(claim.getId());
            logger.info("\t\t migrated successfully claim: " + claim.getReferenceNumber());
            logger.info("---------------------------");
        });
    }
}

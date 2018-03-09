package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.ccd.services.CoreCaseDataService;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

        User user = userService.getUser(userService.authenticateSystemUpdateUser());
        List<Claim> notMigratedClaims = claimRepository.getAllNotMigratedClaims();

        logger.info("User token: " + user.getAuthorisation());

        AtomicInteger migratedClaims = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedMigrations = new AtomicInteger(0);

        notMigratedClaims.forEach(claim -> {
            try {
                logger.info("\t\t start migrating claim: " + claim.getReferenceNumber());

                Optional<Long> ccdId = coreCaseDataService.getCcdIdByReferenceNumber(user, claim.getReferenceNumber());
                if (ccdId.isPresent()) {
                    coreCaseDataService.overwrite(user, ccdId.get(), claim);
                    logger.info("\t\t claim exists - overwrite");
                    updatedClaims.incrementAndGet();
                } else {
                    coreCaseDataService.create(user, claim);
                    logger.info("\t\t claim created in ccd");
                    migratedClaims.incrementAndGet();
                }

                claimRepository.markAsMigrated(claim.getId());

                logger.info("\t\t migrated successfully claim: " + claim.getReferenceNumber());
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
                failedMigrations.incrementAndGet();
            }
        });

        logger.info("Total Claims in database: " + notMigratedClaims.size());
        logger.info("Successfully migrated: " + migratedClaims.toString());
        logger.info("Successfully updated: " + updatedClaims.toString());
        logger.info("Failed to migrate: " + failedMigrations.toString());
    }
}

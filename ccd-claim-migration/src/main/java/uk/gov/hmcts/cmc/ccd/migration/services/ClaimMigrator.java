package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClaimMigrator {

    private static final Logger logger = LoggerFactory.getLogger(ClaimMigrator.class);

    private ClaimRepository claimRepository;
    private UserService userService;
    private final MigrationHandler migrationHandler;

    @Autowired
    public ClaimMigrator(
        ClaimRepository claimRepository,
        UserService userService,
        MigrationHandler migrationHandler
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.migrationHandler = migrationHandler;
    }

    @LogExecutionTime
    public void migrate() {
        logger.info("===== MIGRATE CLAIMS TO CCD =====");

        User user = userService.authenticateSystemUpdateUser();
        List<Claim> notMigratedClaims = claimRepository.getAllNotMigratedClaims();

        logger.info("User token: " + user.getAuthorisation());

        AtomicInteger migratedClaims = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedMigrations = new AtomicInteger(0);

        ForkJoinPool forkJoinPool = new ForkJoinPool(50);

        try {
            forkJoinPool
                .submit(() -> migrateClaims(user, notMigratedClaims, migratedClaims, updatedClaims, failedMigrations))
                .get();
        } catch (InterruptedException | ExecutionException e) {
            // handle exceptions
        } finally {
            forkJoinPool.shutdown();
        }

        logger.info("Total Claims in database: " + notMigratedClaims.size());
        logger.info("Successfully migrated: " + migratedClaims.toString());
        logger.info("Successfully updated: " + updatedClaims.toString());
        logger.info("Failed to migrate: " + failedMigrations.toString());
    }

    private void migrateClaims(
        User user,
        List<Claim> notMigratedClaims,
        AtomicInteger migratedClaims,
        AtomicInteger updatedClaims,
        AtomicInteger failedMigrations
    ) {
        notMigratedClaims.parallelStream().forEach(claim -> {
            migrationHandler.migrateClaim(migratedClaims, failedMigrations, updatedClaims, claim, user);
        });
    }
}

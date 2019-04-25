package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

    private final ClaimRepository claimRepository;
    private final UserService userService;
    private final MigrationHandler migrationHandler;
    private final List<String> casesToMigrate;

    @Autowired
    public ClaimMigrator(
        ClaimRepository claimRepository,
        UserService userService,
        MigrationHandler migrationHandler,
        @Value("${migration.cases.references}") List<String> casesToMigrate

    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.migrationHandler = migrationHandler;
        this.casesToMigrate = casesToMigrate;
    }

    @LogExecutionTime
    public void migrate() {
        logger.info("===== MIGRATE CLAIMS TO CCD =====");

        User user = userService.authenticateSystemUpdateUser();
        List<Claim> claimsToMigrate = getClaimsToMigrate();

        logger.info("User token: " + user.getAuthorisation());

        AtomicInteger migratedClaims = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedMigrations = new AtomicInteger(0);

        ForkJoinPool forkJoinPool = new ForkJoinPool(25);

        try {
            forkJoinPool
                .submit(() -> migrateClaims(user, claimsToMigrate, migratedClaims, updatedClaims, failedMigrations))
                .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("failed migration due to fork join pool interruption");
        } finally {
            forkJoinPool.shutdown();
        }

        logger.info("Total Claims in database: " + claimsToMigrate.size());
        logger.info("Successful creates: " + migratedClaims.toString());
        logger.info("Successful updates: " + updatedClaims.toString());
        logger.info("Total ccd calls: " + (updatedClaims.intValue() + migratedClaims.intValue()));
        logger.info("Failed ccd calls: " + failedMigrations.toString());
    }

    private List<Claim> getClaimsToMigrate() {
        if (CollectionUtils.isEmpty(casesToMigrate)) {
            return claimRepository.getAllNotMigratedClaims();
        } else {
            return claimRepository.getClaims(casesToMigrate);
        }
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

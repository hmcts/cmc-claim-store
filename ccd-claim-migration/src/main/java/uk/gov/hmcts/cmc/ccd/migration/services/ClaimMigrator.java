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
import uk.gov.hmcts.cmc.domain.models.Interest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT;

@Service
public class ClaimMigrator {

    private static final Logger logger = LoggerFactory.getLogger(ClaimMigrator.class);

    private final ClaimRepository claimRepository;
    private final UserService userService;
    private final MigrationHandler migrationHandler;
    private final DataFixHandler dataFixHandler;
    private final DataFixService dataFixService;
    private final List<String> casesToMigrate;
    private final boolean fixDataIssues;
    private final boolean dryRun;

    @Autowired
    public ClaimMigrator(
        ClaimRepository claimRepository,
        UserService userService,
        MigrationHandler migrationHandler,
        DataFixHandler dataFixHandler,
        DataFixService dataFixService,
        @Value("${migration.cases.references}") List<String> casesToMigrate,
        @Value("${migration.fixDataIssues}") boolean fixDataIssues,
        @Value("${migration.dryRun}") boolean dryRun
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.migrationHandler = migrationHandler;
        this.dataFixHandler = dataFixHandler;
        this.dataFixService = dataFixService;
        this.casesToMigrate = casesToMigrate;
        this.fixDataIssues = fixDataIssues;
        this.dryRun = dryRun;
    }

    @LogExecutionTime
    public void migrate() {
        logger.info("===== MIGRATE CLAIMS TO CCD =====");
        logger.info("DRY RUN Enabled: " + dryRun);

        User user = userService.authenticateSystemUpdateUser();
        List<Claim> claimsToMigrate = getClaimsToMigrate();

        AtomicInteger migratedClaims = new AtomicInteger(0);
        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedOnCreateMigrations = new AtomicInteger(0);
        AtomicInteger failedOnUpdateMigrations = new AtomicInteger(0);

        ForkJoinPool forkJoinPool = new ForkJoinPool(1);

        try {
            forkJoinPool
                .submit(() -> migrateClaims(
                    user,
                    claimsToMigrate,
                    migratedClaims,
                    updatedClaims,
                    failedOnCreateMigrations,
                    failedOnUpdateMigrations
                    )
                )
                .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed migration due to fork join pool interruption");
        } finally {
            forkJoinPool.shutdown();
        }

        logger.info("Total Claims in database: " + claimsToMigrate.size());
        logger.info("Successful creates: " + migratedClaims.toString());
        logger.info("Successful updates: " + updatedClaims.toString());
        logger.info("Total ccd calls: " + (updatedClaims.intValue() + migratedClaims.intValue()));
        logger.info("Failed on update ccd calls: " + failedOnUpdateMigrations.toString());
        logger.info("Failed on create ccd calls: " + failedOnCreateMigrations.toString());
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
        AtomicInteger failedOnCreateMigrations,
        AtomicInteger failedOnUpdateMigrations
    ) {
        notMigratedClaims.parallelStream().forEach(claim -> {
            if (fixDataIssues) {
                dataFixService.fixClaimFromSecondLastEvent(
                    updatedClaims,
                    failedOnUpdateMigrations,
                    claim,
                    user
                );

            } else {
                migrationHandler.migrateClaim(
                    migratedClaims,
                    failedOnCreateMigrations,
                    failedOnUpdateMigrations,
                    updatedClaims,
                    claim,
                    user
                );
            }
        });

    }

    private boolean isSettledOrJudgement(Claim claim) {
        Interest interest = claim.getClaimData().getInterest();
        return interest != null
            && interest.getInterestDate() != null
            && interest.getInterestDate().getEndDateType() != null
            && interest.getInterestDate().getEndDateType() == SETTLED_OR_JUDGMENT;
    }
}

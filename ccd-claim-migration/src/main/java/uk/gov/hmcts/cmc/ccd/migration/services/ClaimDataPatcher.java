package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class ClaimDataPatcher {

    private static final Logger logger = LoggerFactory.getLogger(ClaimDataPatcher.class);

    private final ClaimRepository claimRepository;
    private final UserService userService;
    private final DataFixService dataFixService;
    private final List<String> casesToMigrate;
    private final boolean fixDataIssues;
    private final boolean dryRun;

    @Autowired
    public ClaimDataPatcher(
        ClaimRepository claimRepository,
        UserService userService,
        DataFixService dataFixService,
        @Value("${migration.cases.references}") List<String> casesToMigrate,
        @Value("${migration.fixDataIssues}") boolean fixDataIssues,
        @Value("${migration.dryRun}") boolean dryRun
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.dataFixService = dataFixService;
        this.casesToMigrate = casesToMigrate;
        this.fixDataIssues = fixDataIssues;
        this.dryRun = dryRun;
    }

    @LogExecutionTime
    public void patchClaims() {
        logger.info("===== PATCH DATA IN CCD =====");
        logger.info("DRY RUN Enabled: " + dryRun);

        User user = userService.authenticateSystemUpdateUser();
        List<Claim> claimsToMigrate = getClaimsToMigrate();

        AtomicInteger updatedClaims = new AtomicInteger(0);
        AtomicInteger failedOnUpdate = new AtomicInteger(0);

        ForkJoinPool forkJoinPool = new ForkJoinPool(1);

        try {
            forkJoinPool
                .submit(() -> patchClaims(
                    user,
                    claimsToMigrate,
                    updatedClaims,
                    failedOnUpdate
                    )
                )
                .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed migration due to fork join pool interruption");
        } finally {
            forkJoinPool.shutdown();
        }

        logger.info("Total Claims fetched: " + claimsToMigrate.size());
        logger.info("Successful updates: " + updatedClaims.toString());
        logger.info("Failed on update ccd calls: " + failedOnUpdate.toString());
    }

    private List<Claim> getClaimsToMigrate() {
        return claimRepository.getClaims(casesToMigrate);
    }

    private void patchClaims(
        User user,
        List<Claim> notMigratedClaims,
        AtomicInteger updatedClaims,
        AtomicInteger failedOnUpdate
    ) {
        notMigratedClaims.parallelStream().forEach(claim -> {
            if (fixDataIssues) {
                dataFixService.fixClaimFromSecondLastEvent(
                    updatedClaims,
                    failedOnUpdate,
                    claim,
                    user
                );
            }
        });

    }
}

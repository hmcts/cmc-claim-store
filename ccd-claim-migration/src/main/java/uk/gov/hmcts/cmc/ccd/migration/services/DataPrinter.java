package uk.gov.hmcts.cmc.ccd.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.services.UserService;
import uk.gov.hmcts.cmc.ccd.migration.stereotypes.LogExecutionTime;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@Service
public class DataPrinter {

    private static final Logger logger = LoggerFactory.getLogger(DataPrinter.class);

    private final UserService userService;
    private final DataPrintService dataPrintService;
    private final List<String> references;

    @Autowired
    public DataPrinter(
        UserService userService,
        DataPrintService dataPrintService,
        @Value("${migration.cases.references}") List<String> references
    ) {
        this.userService = userService;
        this.dataPrintService = dataPrintService;
        this.references = references;
    }

    @LogExecutionTime
    public void printClaimDetails() {
        logger.info("===== DATA FETCH TO PRINT FROM CCD =====");
        User user = userService.authenticateSystemUpdateUser();

        ForkJoinPool forkJoinPool = new ForkJoinPool(1);

        try {
            forkJoinPool
                .submit(() -> printClaimDetails(user, references))
                .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed migration due to fork join pool interruption");
        } finally {
            forkJoinPool.shutdown();
        }
    }

    private void printClaimDetails(
        User user,
        List<String> references
    ) {
        references.parallelStream().forEach(reference -> {
//            dataPrintService.printCaseDetails(reference, user);
        });

    }
}

package uk.gov.hmcts.cmc.ccd.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.cmc.ccd.migration.services.ClaimDataPatcher;

/**
 * Application to patchClaims claims from claim-store database to CCD datastore.
 */
@SpringBootApplication(scanBasePackages = {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.cmc.ccd.migration",
    "uk.gov.hmcts.cmc.ccd_adapter.mapper"
})
@SuppressWarnings(
    {"HideUtilityClassConstructor", "squid:S1118"}
)
// Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.cmc.ccd.migration.idam.api",
        "uk.gov.hmcts.cmc.ccd.migration.client",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.ccd.client"
    }
)
@EnableRetry
public class App implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Autowired
    private ClaimDataPatcher claimDataPatcher;

    public static void main(String[] args) {
        logger.info("Application started");
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        logger.info("Claims patching is starting");
        claimDataPatcher.patchClaims();
    }
}

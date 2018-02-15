package uk.gov.hmcts.cmc.ccd.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import uk.gov.hmcts.cmc.ccd.migration.services.ClaimMigrator;

/**
 * Application to migrate claims from claim-store database to CCD datastore.
 */
@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.authorisation", "uk.gov.hmcts.cmc.ccd.migration", "uk.gov.hmcts.cmc.ccd.mapper"})
@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.cmc.ccd.migration.idam.api",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.ccd.client"
    }
)
public class App implements CommandLineRunner {

    @Autowired
    private ClaimMigrator claimMigrator;

    public static void main(String[] args) throws Exception {
        System.out.print("Application startup");
        SpringApplication.run(App.class, args);
    }

    //access command line arguments
    @Override
    public void run(String... args) throws Exception {

        claimMigrator.migrate();
    }
}

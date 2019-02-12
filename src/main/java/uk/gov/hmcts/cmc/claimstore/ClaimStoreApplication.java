package uk.gov.hmcts.cmc.claimstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.cmc.claimstore.jobs.NotificationEmailJob;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        DataSourcePoolMetricsAutoConfiguration.class
    },
    scanBasePackages = "uk.gov.hmcts"
)
@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {"uk.gov.hmcts.cmc.claimstore",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.sendletter"
    })
public class ClaimStoreApplication {

    public static final String BASE_PACKAGE_NAME = ClaimStoreApplication.class.getPackage().getName();
    private static final Logger logger = LoggerFactory.getLogger(ClaimStoreApplication.class);

    public static void main(String[] args) {
        logger.info("STARTING CLAIM-STORE...");
        SpringApplication.run(ClaimStoreApplication.class, args);
    }
}

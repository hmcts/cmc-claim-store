package uk.gov.hmcts.cmc.claimstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = "uk.gov.hmcts")
@SuppressWarnings({"HideUtilityClassConstructor", "squid:S1118"}) // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {"uk.gov.hmcts.cmc.claimstore",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.sendletter"
    })
public class ClaimStoreApplication {


    @Autowired
    private AppInsights appInsights;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStoreApplication.class);

    public static final String BASE_PACKAGE_NAME = ClaimStoreApplication.class.getPackage().getName();

    public static void main(String[] args) {
        SpringApplication.run(ClaimStoreApplication.class, args);
    }

    public void run(String... args) throws InterruptedException {
        LOGGER.info("Tracking events");
        trackEvent(AppInsightsEvent.PAID_IN_FULL, 1103);
        LOGGER.info("Done");
    }

    private void trackEvent(AppInsightsEvent event, int number) throws InterruptedException {
        LOGGER.info("Tracking {} count {}", event, number);
        for (int i = 0; i < number; i++) {
            appInsights.trackEvent(event,REFERENCE_NUMBER, null);
            Thread.sleep(200);
        }
    }
}

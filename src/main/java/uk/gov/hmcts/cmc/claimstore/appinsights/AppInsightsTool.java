package uk.gov.hmcts.cmc.claimstore.appinsights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.ClaimStoreApplication;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Component
public class AppInsightsTool implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInsightsTool.class);

    @Autowired
    private AppInsights appInsights;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Tracking events");
        trackEvent(AppInsightsEvent.PAID_IN_FULL, 10); //1103);
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

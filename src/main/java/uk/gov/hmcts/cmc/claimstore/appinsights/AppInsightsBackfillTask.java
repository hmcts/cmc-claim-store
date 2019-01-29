package uk.gov.hmcts.cmc.claimstore.appinsights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

// To back fill 'paid in full' custom events on App Insight
@Component
@ConditionalOnProperty(name = "appinsightstool", havingValue = "true")
public class AppInsightsBackfillTask implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppInsightsBackfillTask.class);

    @Autowired
    private AppInsights appInsights;

    // First 1485 'paid in full' claims being back filled
    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Tracking events");
        trackEvent(AppInsightsEvent.PAID_IN_FULL, 1485);
        LOGGER.info("Done");
    }

    private void trackEvent(AppInsightsEvent event, int number) throws InterruptedException {
        LOGGER.info("Tracking {} count {}", event, number);
        for (int i = 0; i < number; i++) {
            appInsights.trackEvent(event, REFERENCE_NUMBER, null);
            Thread.sleep(200);
        }
    }
}

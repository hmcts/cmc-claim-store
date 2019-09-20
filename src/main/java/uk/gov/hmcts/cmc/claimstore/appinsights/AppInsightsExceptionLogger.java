package uk.gov.hmcts.cmc.claimstore.appinsights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppInsightsExceptionLogger {
    private static final Logger logger = LoggerFactory.getLogger(AppInsightsExceptionLogger.class);
    private final AppInsights appInsights;

    @Autowired
    public AppInsightsExceptionLogger(AppInsights appInsights) {
        this.appInsights = appInsights;
    }

    public void error(Exception exception) {
        logger.error(exception.getMessage(), exception);
        appInsights.trackException(exception);
    }

    public void warn(String message, Exception exception) {
        logger.warn(message, exception);
        appInsights.trackException(exception);
    }

    public void debug(Exception exception) {
        logger.debug(exception.getMessage(), exception);
        appInsights.trackException(exception);
    }

    public void trace(Exception exception) {
        logger.trace(exception.getMessage(), exception);
        appInsights.trackException(exception);
    }
}

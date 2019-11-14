package uk.gov.hmcts.cmc.claimstore.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.IntentionToProceedService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ManagedResource(
    objectName = "JMX_MANAGED_OPERATIONS:category=MBeans,name=IntentionToProceed",
    description = "Managed Bean")
@Component
public class IntentionToProceedMBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IntentionToProceedService intentionToProceedService;

    public IntentionToProceedMBean(IntentionToProceedService intentionToProceedService) {
        this.intentionToProceedService = intentionToProceedService;
    }

    @ManagedOperation
    public void checkClaimsPastIntentionToProceedDeadline(LocalDateTime runDateTime, User user) {
        try {

            LocalDateTime now = LocalDateTime.now();
            logger.info(String.format("checkClaimsPastIntentionToProceedDeadline called for date: %s", now));

            intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(runDateTime, user);

        } catch (Exception e) {
            logger.error("Error triggering stayClaim via jmx", e);
        }
    }

    @ManagedOperation(description = "DateTime to be provided in format 'yyyy-MM-dd HH:mm:ss'")
    public void checkClaimsPastIntentionToProceedDeadline(String dateTime, LocalDateTime runDateTime, User user ) {
        try {

            LocalDateTime localDateTime = LocalDateTime.parse(dateTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            logger.info(String.format("checkClaimsPastIntentionToProceedDeadline called for date: %s", localDateTime));

            intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(runDateTime, user);

        } catch (Exception e) {
            logger.error("Error triggering stayClaim via jmx", e);
        }
    }
}

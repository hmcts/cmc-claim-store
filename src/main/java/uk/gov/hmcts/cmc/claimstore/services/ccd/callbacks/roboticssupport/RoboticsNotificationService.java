package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport;

public interface RoboticsNotificationService {

    String rpaClaimNotification(String referenceNumber);

    String rpaMoreTimeNotifications(String referenceNumber);

    String rpaResponseNotifications(String referenceNumber);

    String rpaCCJNotifications(String referenceNumber);

    String rpaPIFNotifications(String referenceNumber);
}

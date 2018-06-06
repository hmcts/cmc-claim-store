package uk.gov.hmcts.cmc.scheduler.services;

import java.util.Map;

public interface ResponseNeededNotification {
    void sendMail(Map<String, Object> emailData);
}

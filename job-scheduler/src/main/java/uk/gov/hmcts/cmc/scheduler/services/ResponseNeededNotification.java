package uk.gov.hmcts.cmc.scheduler.services;

import java.time.LocalDate;

public interface ResponseNeededNotification {
    void sendMail(
        String targetEmail,
        String emailTemplateId,
        String reference,
        String submitterName,
        String defendantName,
        LocalDate responseDeadline,
        String externalId
    );
}

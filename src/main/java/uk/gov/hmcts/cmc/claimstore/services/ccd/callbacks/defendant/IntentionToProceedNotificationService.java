package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedDeadlineContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.Collections;
import java.util.Map;

@Service
public class IntentionToProceedNotificationService {

    private final StaffEmailProperties staffEmailProperties;
    private final IntentionToProceedContentProvider emailContentProvider;
    private final EmailService emailService;

    @Autowired
    public IntentionToProceedNotificationService(StaffEmailProperties staffEmailProperties,
                                                 IntentionToProceedContentProvider emailContentProvider,
                                                 EmailService emailService) {
        this.staffEmailProperties = staffEmailProperties;
        this.emailContentProvider = emailContentProvider;
        this.emailService = emailService;
    }

    public void notifyCaseworkers(Claim claim) {

        if (FeaturesUtils.isOnlineDQ(claim)) {
            // Caseworkers only want to be notified for offline DQs
            return;
        }

        Map<String, Object> parameters = IntentionToProceedDeadlineContentProvider.getParameters(claim);
        EmailContent emailContent = emailContentProvider.createContent(parameters);
        emailService.sendEmail(
            staffEmailProperties.getSender(),
            new EmailData(
                staffEmailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                Collections.emptyList()
            ));
    }
}

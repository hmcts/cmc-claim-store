package uk.gov.hmcts.cmc.claimstore.events.settlement;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementCountersignedEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class CountersignSettlementAgreementActionsHandler {
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final StaffEmailProperties staffEmailProperties;
    private final EmailService emailService;
    private final SettlementCountersignedEmailContentProvider settlementCountersignedEmailContentProvider;

    @Autowired
    public CountersignSettlementAgreementActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        StaffEmailProperties staffEmailProperties,
        EmailService emailService,
        SettlementCountersignedEmailContentProvider settlementCountersignedEmailContentProvider
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.staffEmailProperties = staffEmailProperties;
        this.emailService = emailService;
        this.settlementCountersignedEmailContentProvider = settlementCountersignedEmailContentProvider;
    }

    @EventListener
    public void sendNotificationToClaimant(CountersignSettlementAgreementEvent event) {
        final Claim claim = event.getClaim();
        final Map<String, String> parameters = aggregateParams(claim);
        final String referenceNumber = claim.getReferenceNumber();
        this.notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantSignedSettlementAgreementToClaimant(),
            parameters,
            NotificationReferenceBuilder.AgreementCounterSigned.referenceForClaimant(referenceNumber,
                NotificationReferenceBuilder.DEFENDANT)
        );
    }

    @EventListener
    public void sendNotificationToDefendant(CountersignSettlementAgreementEvent event) {
        final Claim claim = event.getClaim();
        final Map<String, String> parameters = aggregateParams(claim);
        final String referenceNumber = claim.getReferenceNumber();
        this.notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantSignedSettlementAgreementToDefendant(),
            parameters,
            NotificationReferenceBuilder.AgreementCounterSigned.referenceForDefendant(referenceNumber,
                NotificationReferenceBuilder.DEFENDANT)
        );
    }

    @EventListener
    public void sendNotificationToStaff(CountersignSettlementAgreementEvent event) {
        final Claim claim = event.getClaim();
        final Map<String, Object> parameters = aggregateParameters(claim);
        EmailContent emailcontent = settlementCountersignedEmailContentProvider.createContent(parameters);
        this.emailService.sendEmail(
            staffEmailProperties.getSender(),
            new EmailData(staffEmailProperties.getRecipient(), emailcontent.getSubject(), emailcontent.getBody(),
                Collections.emptyList()));
    }

    private Map<String, Object> aggregateParameters(Claim claim) {
        return ImmutableMap.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber()
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return ImmutableMap.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl(),
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber()
        );
    }

}

package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantAddressUpdateEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimDefendantComparator;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantAddressChangeStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.Collections;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class DefendantAddressChangeStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final DefendantAddressChangeStaffNotificationEmailContentProvider emailContentProvider;

    @Autowired
    public DefendantAddressChangeStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        DefendantAddressChangeStaffNotificationEmailContentProvider emailContentProvider
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
    }

    @EventListener
    public void notifyStaffOfDefendantAddressChange(DefendantAddressUpdateEvent event) {
        ClaimDefendantComparator comparator = new ClaimDefendantComparator(
            event.getClaim(), event.getDefendant()
        );

        //if no details were changed, do nothing
        if (comparator.isEqual()) {
            return;
        }

        EmailData emailData = prepareEmailData(event.getClaim(), comparator);
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim, ClaimDefendantComparator comparator) {
        EmailContent content = emailContentProvider.createContent(claim, comparator);

        return new EmailData(emailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            Collections.emptyList()
        );
    }
}

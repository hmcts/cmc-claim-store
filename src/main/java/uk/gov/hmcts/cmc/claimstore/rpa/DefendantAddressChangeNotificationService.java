package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantAddressUpdateEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.rpa.email.DefendantAddressChangeEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.ClaimDefendantComparator;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.SealedClaimJsonMapper;

@Service("rpa/defendant-address-change-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class DefendantAddressChangeNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final DefendantAddressChangeEmailContentProvider emailContentProvider;
    private final SealedClaimJsonMapper jsonMapper;

    @Autowired
    public DefendantAddressChangeNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        DefendantAddressChangeEmailContentProvider emailContentProvider,
        SealedClaimJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotOfDefendantAddressChange(DefendantAddressUpdateEvent event) {
        ClaimDefendantComparator comparator = new ClaimDefendantComparator(
            event.getClaim(), event.getDefendant()
        );

        //if any of addresses didn't change, do nothing
        if (comparator.isDefendantAddressEqual() && comparator.isDefendantCorrespondenceAddressEqual()) {
            return;
        }

        EmailData emailData = prepareEmailData(event.getClaim(), comparator);
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim, ClaimDefendantComparator comparator) {
        EmailContent content = emailContentProvider.createContent(comparator);

        return new EmailData(emailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            Lists.newArrayList(createSealedClaimJsonAttachment(claim))
        );
    }

    private EmailAttachment createSealedClaimJsonAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

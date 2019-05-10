package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.MoreTimeRequestedJsonMapper;

import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;

@Service("rpa/more-time-requested-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff", havingValue = "true")
public class MoreTimeRequestedNotificationService {

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final MoreTimeRequestedJsonMapper jsonMapper;

    @Autowired
    public MoreTimeRequestedNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        MoreTimeRequestedJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotics(MoreTimeRequestedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        return new EmailData(emailProperties.getMoreTimeRequestedRecipient(),
            "J additional time " + claim.getReferenceNumber(),
            "",
            Collections.singletonList(createMoreTimeRequestedAttachment(claim))
        );
    }

    private EmailAttachment createMoreTimeRequestedAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonMoreTimeRequestedFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.DefenceResponseJsonMapper;

import java.util.Collections;

import static java.util.Objects.requireNonNull;

@Service("rpa/claim-issued-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class DefenceResponseNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final DefenceResponseJsonMapper jsonMapper;

    @Autowired
    public DefenceResponseNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        DefenceResponseJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotics(DocumentGeneratedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {

        return new EmailData(emailProperties.getRecipient(),
            "J defence response " + claim.getReferenceNumber(),
            "",
            Collections.singletonList(createDefenceResponseAttachment(claim))
        );
    }

    private EmailAttachment createDefenceResponseAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonDefenceResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.DefenceResponseJsonMapper;

import java.util.Collections;

import static java.util.Objects.requireNonNull;

@Service("rpa/defendant-response-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class DefenceResponseNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final DefenceResponseJsonMapper responseJsonMapper;

    @Autowired
    public DefenceResponseNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        DefenceResponseJsonMapper responseJsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.responseJsonMapper = responseJsonMapper;

    }

    @EventListener
    public void notifyRobotics(DefendantResponseEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        return new EmailData(emailProperties.getResponseRecipient(),
            "J defence response " + claim.getReferenceNumber(),
            "",
            Collections.singletonList(createResponseJsonAttachment(claim))
        );
    }

    private EmailAttachment createResponseJsonAttachment(Claim claim) {
        return EmailAttachment.json(responseJsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonDefenceResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

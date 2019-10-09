package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.PaidInFullJsonMapper;

import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;

@Service("rpa/paid-in-full-notification-service")
public class PaidInFullNotificationService {

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final PaidInFullJsonMapper jsonMapper;

    @Autowired
    public PaidInFullNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        PaidInFullJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotics(PaidInFullEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        return new EmailData(emailProperties.getPaidInFullRecipient(),
            "J paid in full " + claim.getReferenceNumber(),
            "",
            Collections.singletonList(createPaidInFullAttachment(claim))
        );
    }

    private EmailAttachment createPaidInFullAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonPaidInFullFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

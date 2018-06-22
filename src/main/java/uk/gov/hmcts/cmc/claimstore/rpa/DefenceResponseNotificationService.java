package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.DefenceResponseJsonMapper;

import static java.util.Objects.requireNonNull;

@Service("rpa/defendant-response-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class DefenceResponseNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final DefenceResponseJsonMapper responseJsonMapper;
    private final DefendantResponseStaffNotificationService defendantResponseStaffNotificationService;

    @Autowired
    public DefenceResponseNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        DefenceResponseJsonMapper responseJsonMapper,
        DefendantResponseStaffNotificationService defendantResponseStaffNotificationService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.responseJsonMapper = responseJsonMapper;
        this.defendantResponseStaffNotificationService = defendantResponseStaffNotificationService;
    }

    @EventListener
    public void notifyRobotics(DefendantResponseEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        EmailAttachment responsePDFAttachment = defendantResponseStaffNotificationService
            .createResponsePdfAttachment(claim);

        return new EmailData(emailProperties.getResponseRecipient(),
            "J defence response " + claim.getReferenceNumber(),
            "",
            Lists.newArrayList(responsePDFAttachment, createResponseJsonAttachment(claim))
        );
    }

    private EmailAttachment createResponseJsonAttachment(Claim claim) {
        return EmailAttachment.json(responseJsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

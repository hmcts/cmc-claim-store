package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.ClaimantResponseJsonMapper;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;

@Service("rpa/claimant-response-notification-service")
public class ClaimantResponseNotificationService {

    public static final String J_CLAIMANT_RESPONSE = "J claimant response ";
    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final ClaimantResponseJsonMapper responseJsonMapper;

    @Autowired
    public ClaimantResponseNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        ClaimantResponseJsonMapper responseJsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.responseJsonMapper = responseJsonMapper;
    }

    @EventListener
    public void notifyRobotics(ClaimantResponseEvent event) {
        requireNonNull(event);
        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        requireNonNull(claim);
        EmailAttachment responsePDFAttachment = createClaimantResponseAttachment(claim);
        return new EmailData(emailProperties.getClaimantResponseRecipient(),
            J_CLAIMANT_RESPONSE + claim.getReferenceNumber(),
            "",
            Lists.newArrayList(responsePDFAttachment)
        );
    }

    private EmailAttachment createClaimantResponseAttachment(Claim claim) {
        return EmailAttachment.json(responseJsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonClaimantResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}


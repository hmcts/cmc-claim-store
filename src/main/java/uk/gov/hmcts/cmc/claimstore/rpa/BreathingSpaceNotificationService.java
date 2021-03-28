package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.rpa.email.BreathingSpaceEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.BreathingSpaceJsonMapper;

import java.util.List;
import javax.json.JsonObject;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service("rpa/breathing-space-notification-service")
public class BreathingSpaceNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final BreathingSpaceEmailContentProvider emailContentProvider;
    private final BreathingSpaceJsonMapper breathingSpaceJsonMapper;

    @Autowired
    public BreathingSpaceNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        BreathingSpaceEmailContentProvider emailContentProvider,
        BreathingSpaceJsonMapper breathingSpaceJsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.breathingSpaceJsonMapper = breathingSpaceJsonMapper;
    }

    public void notifyRobotics(Claim claim, List<PDF> documents) {
        requireNonNull(claim);

        EmailData emailData = prepareEmailData(claim, documents);
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim, List<PDF> documents) {
        EmailContent content = emailContentProvider.createContent(claim);

        EmailAttachment sealedClaimPdfAttachment = documents.stream()
            .filter(document -> document.getClaimDocumentType() == SEALED_CLAIM)
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Event does not contain sealed claim PDF"));

        return new EmailData(getSealedClaimRecipient(claim),
            content.getSubject(),
            content.getBody(),
            Lists.newArrayList(sealedClaimPdfAttachment, createSealedClaimJsonAttachment(claim))
        );
    }

    private String getSealedClaimRecipient(Claim claim) {

        return emailProperties.getSealedClaimRecipient();
    }

    private EmailAttachment createSealedClaimJsonAttachment(Claim claim) {
        return EmailAttachment.json(mapToJson(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

    private JsonObject mapToJson(Claim claim) {
        return
            breathingSpaceJsonMapper.map(claim);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.RpaEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.SealedClaimJsonMapper;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class ClaimIssuedRpaNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final RpaEmailProperties emailProperties;
    private final ClaimIssuedRpaNotificationEmailContentProvider emailContentProvider;
    private final SealedClaimJsonMapper jsonMapper;

    @Autowired
    public ClaimIssuedRpaNotificationService(
        EmailService emailService,
        RpaEmailProperties emailProperties,
        ClaimIssuedRpaNotificationEmailContentProvider emailContentProvider,
        SealedClaimJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotOfClaimIssue(DocumentGeneratedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim(), event.getDocuments());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim, List<PDF> documents) {
        EmailContent content = emailContentProvider.createContent(claim);

        EmailAttachment sealedClaimPdfAttachment = documents.stream()
            .filter(document -> document.getFilename().contains("claim-form"))
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .findFirst().orElseThrow(() -> new RuntimeException("Cannot find sealed claim PDF"));

        return new EmailData(emailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            Lists.newArrayList(sealedClaimPdfAttachment, createSealedClaimJsonAttachment(claim))
        );
    }

    private EmailAttachment createSealedClaimJsonAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

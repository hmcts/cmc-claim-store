package uk.gov.hmcts.cmc.claimstore.services.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.RpaEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.domain.Case;
import uk.gov.hmcts.cmc.rpa.mapper.CaseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class ClaimIssuedRpaNotificationService {
    static final String JSON_EXTENSION = ".json";
    private final EmailService emailService;
    private final RpaEmailProperties rpaEmailProperties;
    private final ClaimIssuedRpaNotificationEmailContentProvider provider;
    private final CaseMapper rpaCaseMapper;
    private final JsonMapper jsonMapper;

    @Autowired
    public ClaimIssuedRpaNotificationService(
        EmailService emailService,
        RpaEmailProperties rpaEmailProperties,
        ClaimIssuedRpaNotificationEmailContentProvider provider,
        CaseMapper rpaCaseMapper,
        JsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.rpaEmailProperties = rpaEmailProperties;
        this.provider = provider;
        this.rpaCaseMapper = rpaCaseMapper;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    public void notifyRobotOfClaimIssue(DocumentGeneratedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim(), event.getDocuments());
        emailService.sendEmail(rpaEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(
        Claim claim,
        List<PDF> documents) {
        EmailContent content = provider.createContent(wrapInMap(claim));

        List<EmailAttachment> attachments = documents.stream()
            .filter(document -> document.getFilename().contains("claim-form"))
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .collect(Collectors.toList());

        addJsonFileToAttachments(claim, attachments);

        return new EmailData(rpaEmailProperties.getRecipient(),
            content.getSubject(),
            content.getBody(),
            attachments);
    }

    private void addJsonFileToAttachments(Claim claim, List<EmailAttachment> attachments) {
        Case result = rpaCaseMapper.to(claim);

        attachments.add(EmailAttachment.json(jsonMapper.toJson(result).getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION));
    }

    static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        return map;
    }
}

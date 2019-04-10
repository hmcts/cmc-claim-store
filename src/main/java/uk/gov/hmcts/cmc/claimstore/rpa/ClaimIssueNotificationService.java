package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.rpa.email.ClaimIssuedEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.SealedClaimJsonMapper;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service("rpa/claim-issued-notification-service")
public class ClaimIssueNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final ClaimIssuedEmailContentProvider emailContentProvider;
    private final SealedClaimJsonMapper jsonMapper;

    @Autowired
    public ClaimIssueNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        ClaimIssuedEmailContentProvider emailContentProvider,
        SealedClaimJsonMapper jsonMapper
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.jsonMapper = jsonMapper;
    }

    public void notifyRobotics(Claim claim, List<PDF> documents) {
        requireNonNull(claim);

        if (!claim.getClaimData().isClaimantRepresented()) {
            EmailData emailData = prepareEmailData(claim, documents);
            emailService.sendEmail(emailProperties.getSender(), emailData);
        }
    }

    private EmailData prepareEmailData(Claim claim, List<PDF> documents) {
        EmailContent content = emailContentProvider.createContent(claim);

        EmailAttachment sealedClaimPdfAttachment = documents.stream()
            .filter(document -> document.getClaimDocumentType() == SEALED_CLAIM)
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Event does not contain sealed claim PDF"));

        return new EmailData(emailProperties.getSealedClaimRecipient(),
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

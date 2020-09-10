package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.cmc.rpa.mapper.LegalSealedClaimJsonMapper;
import uk.gov.hmcts.cmc.rpa.mapper.SealedClaimJsonMapper;

import java.util.List;
import javax.json.JsonObject;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service("rpa/claim-issued-notification-service")
public class ClaimIssuedNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final ClaimIssuedEmailContentProvider emailContentProvider;
    private final SealedClaimJsonMapper citizenSealedClaimMapper;
    private final LegalSealedClaimJsonMapper legalSealedClaimMapper;
    private boolean legalSealedClaimEnabledForRpa;

    @Autowired
    public ClaimIssuedNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        ClaimIssuedEmailContentProvider emailContentProvider,
        SealedClaimJsonMapper citizenSealedClaimMapper,
        LegalSealedClaimJsonMapper legalSealedClaimMapper,
        @Value("${feature_toggles.legal_sealed_claim_for_rpa_enabled}") boolean legalSealedClaimEnabledForRpa
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.citizenSealedClaimMapper = citizenSealedClaimMapper;
        this.legalSealedClaimMapper = legalSealedClaimMapper;
        this.legalSealedClaimEnabledForRpa = legalSealedClaimEnabledForRpa;
    }

    public void notifyRobotics(Claim claim, List<PDF> documents) {
        requireNonNull(claim);

        if (claim.getClaimData().isClaimantRepresented() && !legalSealedClaimEnabledForRpa) {
            return;
        }

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

        return claim.getClaimData().isClaimantRepresented()
            ? emailProperties.getLegalSealedClaimRecipient()
            : emailProperties.getSealedClaimRecipient();
    }

    private EmailAttachment createSealedClaimJsonAttachment(Claim claim) {
        return EmailAttachment.json(mapToJson(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

    private JsonObject mapToJson(Claim claim) {
        return claim.getClaimData().isClaimantRepresented()
            ? legalSealedClaimMapper.map(claim)
            : citizenSealedClaimMapper.map(claim);
    }

}

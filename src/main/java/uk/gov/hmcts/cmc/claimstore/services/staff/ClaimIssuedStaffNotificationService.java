package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class ClaimIssuedStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final ClaimIssuedStaffNotificationEmailContentProvider provider;
    private final boolean staffEmailsEnabled;
    private final boolean staffEmailsEnabledForLegalRep;

    @Autowired
    public ClaimIssuedStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        ClaimIssuedStaffNotificationEmailContentProvider provider,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled,
        @Value("${feature_toggles.staff_emails_enabled_for_legal_rep}") boolean staffEmailsEnabledForLegalRep
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.staffEmailsEnabled = staffEmailsEnabled;
        this.staffEmailsEnabledForLegalRep = staffEmailsEnabledForLegalRep;
    }

    @LogExecutionTime
    public void notifyStaffOfClaimIssue(Claim claim, List<PDF> documents) {

        if (staffEmailsEnabledForLegalRep && !claim.getClaimData().isClaimantRepresented()) {
            requireNonNull(claim);
            EmailData emailData = prepareEmailDataForLegalRep(claim, documents);
            emailService.sendEmail(staffEmailProperties.getSender(), emailData);
        }
    }

    private EmailData prepareEmailDataForLegalRep(Claim claim, List<PDF> documents) {
        EmailContent content = provider.createContent(wrapInMap(claim));
        List<EmailAttachment> attachments = documents.stream()
            .filter(document -> document.getClaimDocumentType() != CLAIM_ISSUE_RECEIPT)
            .map(document -> pdf(document.getBytes(), document.getFilename()))
            .collect(Collectors.toList());

        return new EmailData(staffEmailProperties.getLegalRecipient(),
            content.getSubject(),
            content.getBody(),
            attachments);
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantRepresented", claim.getClaimData().isClaimantRepresented());
        return map;
    }
}

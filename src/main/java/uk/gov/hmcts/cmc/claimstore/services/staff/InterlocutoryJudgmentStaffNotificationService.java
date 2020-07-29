package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ReDeterminationNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Service
public class InterlocutoryJudgmentStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final StaffPdfCreatorService staffPdfCreatorService;
    private final ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider;
    private final boolean staffEmailsEnabled;

    @Autowired
    public InterlocutoryJudgmentStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        StaffPdfCreatorService staffPdfCreatorService,
        ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.staffPdfCreatorService = staffPdfCreatorService;
        this.reDeterminationNotificationEmailContentProvider = reDeterminationNotificationEmailContentProvider;
        this.staffEmailsEnabled = staffEmailsEnabled;
    }

    public void notifyStaffInterlocutoryJudgmentSubmitted(Claim claim) {
        if (staffEmailsEnabled && claim.getClaimData().isClaimantRepresented()) {
            requireNonNull(claim);
            requireNonNull(claim.getClaimantRespondedAt());

            emailService.sendEmail(
                staffEmailProperties.getSender(),
                prepareInterlocutoryJudgmentEmailData(claim)
            );
        }
    }

    private EmailData prepareInterlocutoryJudgmentEmailData(Claim claim) {
        Map<String, Object> map = createParameterMap(claim);
        EmailContent emailContent = reDeterminationNotificationEmailContentProvider.createContent(map);

        return EmailData.builder()
            .to(staffEmailProperties.getLegalRecipient())
            .subject(emailContent.getSubject())
            .message(emailContent.getBody())
            .attachments(prepareEmailAttachments(claim))
            .build();
    }

    private Map<String, Object> createParameterMap(Claim claim) {
        return ImmutableMap.<String, Object>builder()
            .put("claimReferenceNumber", claim.getReferenceNumber())
            .put("claimantName", claim.getClaimData().getClaimant().getName())
            .put("defendantName", claim.getClaimData().getDefendant().getName())
            .put("partyName", claim.getClaimData().getClaimant().getName())
            .put("interlocutoryJudgement", true)
            .build();
    }

    private List<EmailAttachment> prepareEmailAttachments(Claim claim) {
        ImmutableList.Builder<EmailAttachment> attachments = ImmutableList.builder();
        attachments.add(staffPdfCreatorService.createSealedClaimPdfAttachment(claim));
        attachments.add(staffPdfCreatorService.createResponsePdfAttachment(claim));

        if (claim.getClaimantRespondedAt().isPresent()) {
            attachments.add(staffPdfCreatorService.createClaimantResponsePdfAttachment(claim));
        }
        return attachments.build();
    }

}

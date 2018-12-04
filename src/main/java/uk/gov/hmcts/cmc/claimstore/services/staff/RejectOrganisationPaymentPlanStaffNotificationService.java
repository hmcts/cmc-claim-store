package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RejectOrganisationPaymentPlanStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final StaffPdfCreatorService staffPdfCreatorService;
    private final ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider;

    @Autowired
    public RejectOrganisationPaymentPlanStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        StaffPdfCreatorService staffPdfCreatorService,
        ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.staffPdfCreatorService = staffPdfCreatorService;
        this.reDeterminationNotificationEmailContentProvider = reDeterminationNotificationEmailContentProvider;
    }

    public void notifyStaffClaimantRejectOrganisationPaymentPlan(Claim claim) {
        requireNonNull(claim);
        requireNonNull(claim.getClaimantRespondedAt());

        emailService.sendEmail(
            staffEmailProperties.getSender(),
            prepareClaimantRejectOrganisationPaymentPlanEmailData(claim)
        );
    }

    private EmailData prepareClaimantRejectOrganisationPaymentPlanEmailData(Claim claim) {
        Map<String, Object> map = createParameterMap(claim);
        EmailContent emailContent = reDeterminationNotificationEmailContentProvider.createContent(map);

        return EmailData.builder()
            .to(staffEmailProperties.getRecipient())
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
            .put("interlocutoryJudgement", false)
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

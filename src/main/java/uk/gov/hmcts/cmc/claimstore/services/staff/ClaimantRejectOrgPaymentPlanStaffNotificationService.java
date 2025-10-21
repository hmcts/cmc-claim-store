package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantRejectOrgPaymentPlanStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Service
public class ClaimantRejectOrgPaymentPlanStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final StaffPdfCreatorService staffPdfCreatorService;
    private final ClaimantRejectOrgPaymentPlanStaffEmailContentProvider
        claimantRejectOrgPaymentPlanStaffEmailContentProvider;
    private final boolean staffEmailsEnabled;

    @Autowired
    public ClaimantRejectOrgPaymentPlanStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        StaffPdfCreatorService staffPdfCreatorService,
        ClaimantRejectOrgPaymentPlanStaffEmailContentProvider claimantRejectOrgPaymentPlanStaffEmailContentProvider,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.staffPdfCreatorService = staffPdfCreatorService;
        this.claimantRejectOrgPaymentPlanStaffEmailContentProvider =
            claimantRejectOrgPaymentPlanStaffEmailContentProvider;
        this.staffEmailsEnabled = staffEmailsEnabled;
    }

    public void notifyStaffClaimantRejectOrganisationPaymentPlan(Claim claim) {
        if (staffEmailsEnabled) {

            requireNonNull(claim);
            requireNonNull(claim.getClaimantRespondedAt());

            emailService.sendEmail(
                staffEmailProperties.getSender(),
                prepareClaimantRejectOrganisationPaymentPlanEmailData(claim)
            );
        }
    }

    private EmailData prepareClaimantRejectOrganisationPaymentPlanEmailData(Claim claim) {
        Map<String, Object> map = createParameterMap(claim);
        EmailContent emailContent = claimantRejectOrgPaymentPlanStaffEmailContentProvider.createContent(map);

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

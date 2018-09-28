package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RedeterminationNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@Service
public class CCJStaffNotificationService {

    public static final String FILE_NAME_FORMAT = "%s-%s-county-court-judgment-details.pdf";

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider;
    private final RedeterminationNotificationEmailContentProvider redeterminationNotificationEmailContentProvider;
    private final StaffPdfCreatorService staffPdfCreatorService;

    @Autowired
    public CCJStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider,
        RedeterminationNotificationEmailContentProvider redeterminationNotificationEmailContentProvider,
        StaffPdfCreatorService staffPdfCreatorService
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.ccjRequestSubmittedEmailContentProvider = ccjRequestSubmittedEmailContentProvider;
        this.redeterminationNotificationEmailContentProvider = redeterminationNotificationEmailContentProvider;
        this.staffPdfCreatorService = staffPdfCreatorService;
    }

    public void notifyStaffCCJRequestSubmitted(Claim claim) {
        requireNonNull(claim);
        emailService.sendEmail(staffEmailProperties.getSender(), prepareEmailData(claim));
    }

    private EmailData prepareEmailData(Claim claim) {
        Map<String, Object> map = createParameterMap(claim, null);

        EmailContent emailContent = ccjRequestSubmittedEmailContentProvider.createContent(map);
        return new EmailData(
            staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            singletonList(staffPdfCreatorService.generateCountyCourtJudgmentPdf(claim))
        );
    }

    private Map<String, Object> createParameterMap(Claim claim, String submitterName) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("paymentType", claim.getCountyCourtJudgment().getPaymentOption().getDescription());

        Optional.ofNullable(submitterName).ifPresent(name -> map.put("partyName", name));

        return map;
    }

    public void notifyStaffCCJRedeterminationRequest(Claim claim, String submitterName) {
        requireNonNull(claim);
        emailService.sendEmail(staffEmailProperties.getSender(), prepareReDeterminationEmailData(claim, submitterName));
    }

    private EmailData prepareReDeterminationEmailData(Claim claim, String submitterName) {
        Map<String, Object> map = createParameterMap(claim, submitterName);

        EmailContent emailContent = redeterminationNotificationEmailContentProvider.createContent(map);
        ImmutableList.Builder<EmailAttachment> attachments = ImmutableList.builder();
        attachments.add(staffPdfCreatorService.createSealedClaimPdfAttachment(claim));
        attachments.add(staffPdfCreatorService.createResponsePdfAttachment(claim));

        if (claim.getSettlementReachedAt() != null) {
            attachments.add(staffPdfCreatorService.createSettlementReachedPdfAttachment(claim));
        }

        if (claim.getClaimantRespondedAt().isPresent()) {
            attachments.add(staffPdfCreatorService.createClaimantResponsePdfAttachment(claim));
        }

        return new EmailData(
            staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            attachments.build()
        );
    }
}

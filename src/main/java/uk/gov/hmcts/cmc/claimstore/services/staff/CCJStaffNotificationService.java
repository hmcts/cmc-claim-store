package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ReDeterminationNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper;
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
    private final ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider;
    private final StaffPdfCreatorService staffPdfCreatorService;
    private final boolean staffEmailsEnabled;

    @Autowired
    public CCJStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider,
        ReDeterminationNotificationEmailContentProvider reDeterminationNotificationEmailContentProvider,
        StaffPdfCreatorService staffPdfCreatorService,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailsEnabled
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.ccjRequestSubmittedEmailContentProvider = ccjRequestSubmittedEmailContentProvider;
        this.reDeterminationNotificationEmailContentProvider = reDeterminationNotificationEmailContentProvider;
        this.staffPdfCreatorService = staffPdfCreatorService;
        this.staffEmailsEnabled = staffEmailsEnabled;
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
        map.put("admissionResponse", ResponseHelper.admissionResponse(claim.getResponse().orElse(null)));
        map.put("ccjType", claim.getCountyCourtJudgment().getCcjType().name());
        Optional.ofNullable(submitterName).ifPresent(name -> map.put("partyName", name));
        claim.getReDetermination()
            .ifPresent(reDetermination -> map.put("reasonForReDetermination", reDetermination.getExplanation()));
        return map;
    }

    public void notifyStaffCCJReDeterminationRequest(Claim claim, String submitterName) {
        if (staffEmailsEnabled) {
            requireNonNull(claim);
            emailService.sendEmail(staffEmailProperties.getSender(),
                prepareReDeterminationEmailData(claim, submitterName));
        }
    }

    private EmailData prepareReDeterminationEmailData(Claim claim, String submitterName) {

        ImmutableList.Builder<EmailAttachment> attachments = ImmutableList.builder();
        attachments.add(staffPdfCreatorService.createSealedClaimPdfAttachment(claim));
        attachments.add(staffPdfCreatorService.createResponsePdfAttachment(claim));

        if (claim.getClaimantRespondedAt().isPresent()) {
            attachments.add(staffPdfCreatorService.createClaimantResponsePdfAttachment(claim));
        }

        Map<String, Object> map = createParameterMap(claim, submitterName);
        EmailContent emailContent = reDeterminationNotificationEmailContentProvider.createContent(map);

        return new EmailData(
            staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            attachments.build()
        );
    }
}

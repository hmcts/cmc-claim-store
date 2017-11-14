package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.PdfGenerationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;

@Service
public class ClaimIssuedStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final ClaimIssuedStaffNotificationEmailContentProvider provider;
    private final PdfGenerationService pdfGenerationService;

    @Autowired
    public ClaimIssuedStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final ClaimIssuedStaffNotificationEmailContentProvider provider,
        final PdfGenerationService pdfGenerationService
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.pdfGenerationService = pdfGenerationService;
    }

    public void notifyStaffClaimIssued(final Claim claim,
                                       final Optional<String> defendantPin,
                                       final String submitterEmail,
                                       final String authorisation) {
        requireNonNull(claim);
        requireNonBlank(submitterEmail);
        final EmailData emailData = prepareEmailData(claim, defendantPin, submitterEmail, authorisation);
        emailService.sendEmail(staffEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(final Claim claim,
                                       final Optional<String> defendantPin,
                                       final String submitterEmail,
                                       final String authorisation) {
        EmailContent emailContent = provider.createContent(claim);
        return new EmailData(staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            getAttachments(claim, defendantPin, submitterEmail, authorisation));
    }

    private List<EmailAttachment> getAttachments(
        final Claim claim,
        final Optional<String> defendantPin,
        final String submitterEmail,
        final String authorisation
    ) {
        final List<EmailAttachment> emailAttachments = new ArrayList<>();

        if (!claim.getClaimData().isClaimantRepresented()) {
            String pin = defendantPin.orElseThrow(NullPointerException::new);
            emailAttachments.add(pdfGenerationService.sealedClaimPdf(authorisation, claim, submitterEmail));
            emailAttachments.add(pdfGenerationService.defendantPinLetterPdf(authorisation, claim, pin));
        } else {
            emailAttachments.add(pdfGenerationService.sealedLegalClaimPdf(authorisation, claim));
        }

        return emailAttachments;
    }
}

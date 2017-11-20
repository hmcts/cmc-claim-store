package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.services.SealedClaimDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Preconditions.requireNonBlank;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class ClaimIssuedStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final ClaimIssuedStaffNotificationEmailContentProvider provider;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;
    private final SealedClaimDocumentService sealedClaimDocumentService;

    @Autowired
    public ClaimIssuedStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final ClaimIssuedStaffNotificationEmailContentProvider provider,
        final DefendantPinLetterPdfService defendantPinLetterPdfService,
        final SealedClaimDocumentService sealedClaimDocumentService
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
        this.sealedClaimDocumentService = sealedClaimDocumentService;
    }

    public void notifyStaffClaimIssued(
        final Claim claim,
        final String defendantPin,
        final String submitterEmail
    ) {
        requireNonNull(claim);
        requireNonBlank(submitterEmail);
        final EmailData emailData = prepareEmailData(claim, defendantPin, submitterEmail);
        emailService.sendEmail(staffEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(
        final Claim claim,
        final String defendantPin,
        final String submitterEmail
    ) {
        EmailContent emailContent = provider.createContent(wrapInMap(claim));
        return new EmailData(staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            getAttachments(claim, defendantPin, submitterEmail));
    }

    static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantRepresented", claim.getClaimData().isClaimantRepresented());
        return map;
    }

    private List<EmailAttachment> getAttachments(
        final Claim claim,
        final String defendantPin,
        final String submitterEmail
    ) {
        final List<EmailAttachment> emailAttachments = new ArrayList<>();

        if (!claim.getClaimData().isClaimantRepresented()) {
            String pin = Optional.ofNullable(defendantPin).orElseThrow(NullPointerException::new);
            emailAttachments.add(sealedClaimPdf(claim, submitterEmail));
            emailAttachments.add(defendantPinLetterPdf(claim, pin));
        } else {
            emailAttachments.add(sealedLegalClaimPdf(claim));
        }

        return emailAttachments;
    }

    private EmailAttachment sealedLegalClaimPdf(final Claim claim) {
        final byte[] generatedPdf = sealedClaimDocumentService.generateLegalSealedClaim(
            claim.getExternalId());

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private EmailAttachment sealedClaimPdf(final Claim claim, final String submitterEmail) {
        byte[] generatedPdf = sealedClaimDocumentService.generateCitizenSealedClaim(claim.getExternalId(),
            submitterEmail);

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private EmailAttachment defendantPinLetterPdf(final Claim claim, final String defendantPin) {
        byte[] generatedPdf = defendantPinLetterPdfService.createPdf(claim, defendantPin);

        return pdf(
            generatedPdf,
            format("%s-defendant-pin-letter.pdf", claim.getReferenceNumber())
        );
    }
}

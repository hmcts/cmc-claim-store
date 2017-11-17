package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.SealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

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
    private final PDFServiceClient pdfServiceClient;
    private final SealedClaimContentProvider sealedClaimContentProvider;
    private final DefendantPinLetterContentProvider defendantPinLetterContentProvider;
    private final LegalSealedClaimContentProvider legalSealedClaimContentProvider;

    @Autowired
    public ClaimIssuedStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final ClaimIssuedStaffNotificationEmailContentProvider provider,
        final PDFServiceClient pdfServiceClient,
        final SealedClaimContentProvider sealedClaimContentProvider,
        final DefendantPinLetterContentProvider defendantPinLetterContentProvider,
        final LegalSealedClaimContentProvider legalSealedClaimContentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.pdfServiceClient = pdfServiceClient;
        this.sealedClaimContentProvider = sealedClaimContentProvider;
        this.defendantPinLetterContentProvider = defendantPinLetterContentProvider;
        this.legalSealedClaimContentProvider = legalSealedClaimContentProvider;
    }

    public void notifyStaffClaimIssued(Claim claim, Optional<String> defendantPin, String submitterEmail) {
        requireNonNull(claim);
        requireNonBlank(submitterEmail);
        emailService.sendEmail(staffEmailProperties.getSender(), prepareEmailData(claim, defendantPin, submitterEmail));
    }

    private EmailData prepareEmailData(Claim claim, Optional<String> defendantPin, String submitterEmail) {
        EmailContent emailContent = provider.createContent(wrapInMap(claim));
        return new EmailData(staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            getAttachments(claim, defendantPin, submitterEmail));
    }

    static Map<String, Object> wrapInMap(
        Claim claim
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantRepresented", claim.getClaimData().isClaimantRepresented());
        return map;
    }

    private List<EmailAttachment> getAttachments(
        final Claim claim,
        final Optional<String> defendantPin,
        final String submitterEmail
    ) {
        final List<EmailAttachment> emailAttachments = new ArrayList<>();

        if (!claim.getClaimData().isClaimantRepresented()) {
            String pin = defendantPin.orElseThrow(NullPointerException::new);
            emailAttachments.add(sealedClaimPdf(claim, submitterEmail));
            emailAttachments.add(defendantPinLetterPdf(claim, pin));
        } else {
            emailAttachments.add(sealedLegalClaimPdf(claim));
        }

        return emailAttachments;
    }

    private EmailAttachment sealedLegalClaimPdf(Claim claim) {
        byte[] generatedPdf = pdfServiceClient.generateFromHtml(
            staffEmailProperties.getEmailTemplates().getLegalSealedClaim(),
            legalSealedClaimContentProvider.createContent(claim)
        );

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private EmailAttachment sealedClaimPdf(Claim claim, String submitterEmail) {
        byte[] generatedPdf = pdfServiceClient.generateFromHtml(
            staffEmailProperties.getEmailTemplates().getSealedClaim(),
            sealedClaimContentProvider.createContent(claim, submitterEmail)
        );

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private EmailAttachment defendantPinLetterPdf(Claim claim, String defendantPin) {
        byte[] generatedPdf = pdfServiceClient.generateFromHtml(
            staffEmailProperties.getEmailTemplates().getDefendantPinLetter(),
            defendantPinLetterContentProvider.createContent(claim, defendantPin)
        );
        return pdf(
            generatedPdf,
            format("%s-defendant-pin-letter.pdf", claim.getReferenceNumber())
        );
    }
}

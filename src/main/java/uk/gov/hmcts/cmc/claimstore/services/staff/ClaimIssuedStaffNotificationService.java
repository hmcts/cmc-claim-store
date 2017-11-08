package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import java.util.ArrayList;
import java.util.List;
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
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final DefendantPinLetterContentProvider defendantPinLetterContentProvider;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DocumentManagementService documentManagementService;
    private final boolean dmFeatureToggle;

    @Autowired
    public ClaimIssuedStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final ClaimIssuedStaffNotificationEmailContentProvider provider,
        final PDFServiceClient pdfServiceClient,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final DefendantPinLetterContentProvider defendantPinLetterContentProvider,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final DocumentManagementService documentManagementService,
        @Value("${feature_toggles.document_management}") final boolean dmFeatureToggle
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.pdfServiceClient = pdfServiceClient;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.defendantPinLetterContentProvider = defendantPinLetterContentProvider;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.documentManagementService = documentManagementService;
        this.dmFeatureToggle = dmFeatureToggle;
    }

    public void notifyStaffClaimIssued(Claim claim, Optional<String> defendantPin,
                                       String submitterEmail, String authorisation) throws DocumentManagementException {
        requireNonNull(claim);
        requireNonBlank(submitterEmail);
        final EmailData emailData = prepareEmailData(claim, defendantPin, submitterEmail, authorisation);
        emailService.sendEmail(staffEmailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim, Optional<String> defendantPin,
                                       String submitterEmail, String authorisation) throws DocumentManagementException {
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
    ) throws DocumentManagementException {
        final List<EmailAttachment> emailAttachments = new ArrayList<>();

        if (!claim.getClaimData().isClaimantRepresented()) {
            String pin = defendantPin.orElseThrow(NullPointerException::new);
            emailAttachments.add(sealedClaimPdf(claim, submitterEmail, authorisation));
            emailAttachments.add(defendantPinLetterPdf(claim, pin));
        } else {
            emailAttachments.add(sealedLegalClaimPdf(claim, authorisation));
        }

        return emailAttachments;
    }

    private EmailAttachment sealedLegalClaimPdf(Claim claim, String authorisation) throws DocumentManagementException {
        final byte[] claimPdf = legalSealedClaimPdfService.createPdf(claim);
        byte[] generatedPdf = getPdfDocument(claim, authorisation, claimPdf);

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private byte[] getPdfDocument(final Claim claim, final String authorisation, final byte[] n1ClaimPdf)
        throws DocumentManagementException {
        if (dmFeatureToggle) {
            return n1ClaimPdf;
        } else {
            return documentManagementService.getClaimN1Form(authorisation, claim, n1ClaimPdf);
        }
    }

    private EmailAttachment sealedClaimPdf(Claim claim, String submitterEmail, String authorisation)
        throws DocumentManagementException {
        byte[] claimPdf = citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
        byte[] generatedPdf = getPdfDocument(claim, authorisation, claimPdf);

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

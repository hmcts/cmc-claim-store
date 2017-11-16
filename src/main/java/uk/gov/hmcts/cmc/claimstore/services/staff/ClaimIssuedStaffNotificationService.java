package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentManagementSealedClaimHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

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
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;
    private final DocumentManagementService documentManagementService;
    private final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler;
    private final boolean documentManagementFeatureEnabled;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ClaimIssuedStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final ClaimIssuedStaffNotificationEmailContentProvider provider,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final DefendantPinLetterPdfService defendantPinLetterPdfService,
        final DocumentManagementService documentManagementService,
        final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.provider = provider;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
        this.documentManagementService = documentManagementService;
        this.documentManagementSealedClaimHandler = documentManagementSealedClaimHandler;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
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
            emailAttachments.add(sealedClaimPdf(authorisation, claim, submitterEmail));
            emailAttachments.add(defendantPinLetterPdf(claim, pin));
        } else {
            emailAttachments.add(sealedLegalClaimPdf(authorisation, claim));
        }

        return emailAttachments;
    }

    private EmailAttachment sealedLegalClaimPdf(final String authorisation, final Claim claim) {
        byte[] generatedPdf = getSealedClaimPdfDocument(authorisation, claim, null);

        return pdf(
            generatedPdf,
            format("%s-sealed-claim.pdf", claim.getReferenceNumber())
        );
    }

    private byte[] getSealedClaimPdfDocument(final String authorisation, final Claim claim,
                                             final String submitterEmail) {
        final Optional<String> documentManagementSelfPath = claim.getSealedClaimDocumentManagementSelfPath();

        if (documentManagementFeatureEnabled && documentManagementSelfPath.isPresent()) {
            return documentManagementService.downloadDocument(authorisation, documentManagementSelfPath.get());
        } else {

            if (claim.getClaimData().isClaimantRepresented()) {
                documentManagementSealedClaimHandler.uploadRepresentativeSealedClaimToEvidenceStore(
                    new RepresentedClaimIssuedEvent(claim, Optional.empty(), authorisation)
                );

                return legalSealedClaimPdfService.createPdf(claim);
            } else {
                documentManagementSealedClaimHandler.uploadCitizenSealedClaimToEvidenceStore(
                    new ClaimIssuedEvent(claim, Optional.empty(), Optional.empty(), authorisation)
                );

                return citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
            }
        }
    }

    private EmailAttachment sealedClaimPdf(final String authorisation, final Claim claim, final String submitterEmail) {
        byte[] generatedPdf = getSealedClaimPdfDocument(authorisation, claim, submitterEmail);

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

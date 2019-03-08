package uk.gov.hmcts.cmc.claimstore.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.net.URI;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(DocumentUploadHandler.class);
    private static final String CLAIM_MUST_NOT_BE_NULL = "Claim must not be null";

    private final DocumentsService documentService;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;

    @Autowired
    public DocumentUploadHandler(
        DefendantPinLetterPdfService defendantPinLetterPdfService,
        DocumentsService documentService
    ) {
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
        this.documentService = documentService;
    }

    @EventListener
    public void uploadDocument(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSealedClaim(claim.getExternalId(), event.getAuthorisation());
        documentService.generateClaimIssueReceipt(claim.getExternalId(), event.getAuthorisation());
        generateDefendantPinLetter(claim, event.getPin(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(RepresentedClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSealedClaim(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        claim.getResponse().orElseThrow(() -> new IllegalArgumentException("Response must be present"));
        documentService.generateDefendantResponseReceipt(claim.getExternalId(), event.getAuthorization());
    }

    @EventListener
    public void uploadDocument(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateCountyCourtJudgement(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSettlementAgreement(claim.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    public void uploadDocument(CountersignSettlementAgreementEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        documentService.generateSettlementAgreement(claim.getExternalId(), event.getAuthorisation());
    }

    private void generateDefendantPinLetter(Claim claim, String pin, String authorisation) {
        final String fileName = buildDefendantLetterFileBaseName(claim.getReferenceNumber());
        Optional<URI> claimDocument = claim.getClaimDocument(DEFENDANT_PIN_LETTER);
        if (!claimDocument.isPresent()) {
            try {
                PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
                    defendantPinLetterPdfService.createPdf(claim, pin),
                    DEFENDANT_PIN_LETTER
                );

                documentService.uploadToDocumentManagement(defendantLetter, authorisation, claim);
            } catch (Exception ex) {
                logger.warn(String.format("unable to upload document %s into document management",
                    fileName), ex);
            }
        }
    }

}

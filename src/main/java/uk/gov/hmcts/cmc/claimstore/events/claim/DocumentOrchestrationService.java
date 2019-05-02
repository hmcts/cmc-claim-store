package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class DocumentOrchestrationService {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final ClaimService claimService;
    private final UserService userService;

    public DocumentOrchestrationService(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        PDFServiceClient pdfServiceClient,
        ClaimIssueReceiptService claimIssueReceiptService,
        ClaimService claimService,
        UserService userService
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.pdfServiceClient = pdfServiceClient;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.claimService = claimService;
        this.userService = userService;
    }

    public GeneratedDocuments generateForCitizen(Claim claim, String authorisation) {
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(claim);

        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(
            claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(sealedClaimDoc.template.getBytes(), sealedClaimDoc.values),
            SEALED_CLAIM
        );

        Optional<GeneratePinResponse> pinResponse = getPinResponse(claim.getClaimData(), authorisation);

        String pin = pinResponse
            .map(GeneratePinResponse::getPin)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        Document defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(claim, pin);

        PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values),
            DEFENDANT_PIN_LETTER);

        PDF claimIssueReceipt = new PDF(buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            claimIssueReceiptService.createPdf(claim),
            CLAIM_ISSUE_RECEIPT
        );

        String letterHolderId = pinResponse.map(GeneratePinResponse::getUserId)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        claimService.linkLetterHolder(claim.getId(), letterHolderId);

        return GeneratedDocuments.builder()
            .claimIssueReceipt(claimIssueReceipt)
            .defendantLetter(defendantLetter)
            .sealedClaim(sealedClaim)
            .defendantLetterDoc(defendantLetterDoc)
            .sealedClaimDoc(sealedClaimDoc)
            .pin(pin)
            .build();
    }

    private Optional<GeneratePinResponse> getPinResponse(ClaimData claimData, String authorisation) {
        return Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
    }

    public GeneratedDocuments generateForRepresentative(Claim claim) {
        PDF sealedClaim = new PDF(
            buildSealedClaimFileBaseName(claim.getReferenceNumber()),
            sealedClaimPdfService.createPdf(claim),
            SEALED_CLAIM
        );

        return GeneratedDocuments.builder()
            .sealedClaim(sealedClaim)
            .build();
    }
}

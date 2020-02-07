package uk.gov.hmcts.cmc.claimstore.events.claim;

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

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Service
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
        Optional<GeneratePinResponse> pinResponse = getPinResponse(claim.getClaimData(), authorisation);

        String pin = pinResponse
            .map(GeneratePinResponse::getPin)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        Document defendantPinLetterDoc = citizenServiceDocumentsService.pinLetterDocument(claim, pin);

        PDF defendantPinLetter = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(defendantPinLetterDoc.template.getBytes(), defendantPinLetterDoc.values),
            DEFENDANT_PIN_LETTER);

        String letterHolderId = pinResponse.map(GeneratePinResponse::getUserId)
            .orElseThrow(() -> new IllegalArgumentException("Pin generation failed"));

        Claim updated = claimService.linkLetterHolder(claim, letterHolderId, authorisation);
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(claim);

        return GeneratedDocuments.builder()
            .claimIssueReceipt(claimIssueReceiptService.createPdf(claim))
            .defendantPinLetter(defendantPinLetter)
            .sealedClaim(getClaimPdf(claim, sealedClaimDoc))
            .defendantPinLetterDoc(defendantPinLetterDoc)
            .sealedClaimDoc(sealedClaimDoc)
            .pin(pin)
            .claim(updated)
            .build();
    }

    public PDF getSealedClaimPdf(Claim claim) {
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(claim);
        return getClaimPdf(claim, sealedClaimDoc);
    }

    private PDF getClaimPdf(Claim claim, Document sealedClaimDoc) {
        return new PDF(buildSealedClaimFileBaseName(
            claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(
                sealedClaimDoc.template.getBytes(), sealedClaimDoc.values),
            SEALED_CLAIM
        );
    }

    public PDF getClaimIssueReceiptPdf(Claim claim) {
        return claimIssueReceiptService.createPdf(claim);
    }

    public GeneratedDocuments getSealedClaimForRepresentative(Claim claim) {
        return GeneratedDocuments.builder()
            .sealedClaim(sealedClaimPdfService.createPdf(claim))
            .build();
    }

    private Optional<GeneratePinResponse> getPinResponse(ClaimData claimData, String authorisation) {
        return Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
    }
}

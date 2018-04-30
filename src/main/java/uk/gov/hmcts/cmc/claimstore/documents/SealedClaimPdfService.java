package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static java.util.Objects.requireNonNull;

@Service
public class SealedClaimPdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final LegalSealedClaimContentProvider legalSealedClaimContentProvider;
    private final CitizenServiceDocumentsService citizenServiceDocumentsService;

    @Autowired
    public SealedClaimPdfService(
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient,
        LegalSealedClaimContentProvider legalSealedClaimContentProvider,
        CitizenServiceDocumentsService citizenServiceDocumentsService
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.legalSealedClaimContentProvider = legalSealedClaimContentProvider;
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);

        if (claim.getClaimData().isClaimantRepresented()) {
            return pdfServiceClient.generateFromHtml(
                documentTemplates.getLegalSealedClaim(),
                legalSealedClaimContentProvider.createContent(claim)
            );
        } else {
            Document document = citizenServiceDocumentsService.sealedClaimDocument(claim);
            return pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        }
    }
}

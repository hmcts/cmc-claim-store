package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Service
public class SealedClaimPdfService implements PdfService {

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

    @LogExecutionTime
    public PDF createPdf(Claim claim) {
        requireNonNull(claim);
        byte[] content;

        if (claim.getClaimData().isClaimantRepresented()) {
            content = pdfServiceClient.generateFromHtml(
                documentTemplates.getLegalSealedClaim(),
                legalSealedClaimContentProvider.createContent(claim));
        } else {
            Document document = citizenServiceDocumentsService.sealedClaimDocument(claim);
            content = pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        }
        return new PDF(
            buildSealedClaimFileBaseName(claim.getReferenceNumber()),
            content,
            SEALED_CLAIM
        );
    }
}

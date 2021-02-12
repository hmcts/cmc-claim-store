package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDraftClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DRAFT_CLAIM_RECEIPT;

@Service
public class DraftClaimReceiptService implements PdfService {

    private final PDFServiceClient pdfServiceClient;
    private final CitizenServiceDocumentsService citizenServiceDocumentsService;

    @Autowired
    public DraftClaimReceiptService(
        PDFServiceClient pdfServiceClient,
        CitizenServiceDocumentsService citizenServiceDocumentsService
    ) {
        this.pdfServiceClient = pdfServiceClient;
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
    }

    @LogExecutionTime
    public PDF createPdf(Claim claim) {
        requireNonNull(claim);
        Document document = citizenServiceDocumentsService.draftClaimDocument(claim);
        byte[] content = pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        return new PDF(
            buildDraftClaimFileBaseName(claim.getExternalId()),
            content,
            DRAFT_CLAIM_RECEIPT
        );
    }
}

package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;

@Service
public class CountyCourtJudgmentPdfService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ContentProvider contentProvider;

    @Autowired
    public CountyCourtJudgmentPdfService(
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient,
        ContentProvider contentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.contentProvider = contentProvider;
    }

    @Override
    public PDF createPdf(Claim claim) {
        requireNonNull(claim);

        return new PDF(
            buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()),
            pdfServiceClient.generateFromHtml(
                documentTemplates.getCountyCourtJudgmentByRequest(),
                contentProvider.createContent(claim)),
            ClaimDocumentType.CCJ_REQUEST
        );
    }
}

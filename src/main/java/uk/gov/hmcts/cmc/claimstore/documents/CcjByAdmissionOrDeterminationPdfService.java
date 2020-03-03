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
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementByAdmissionFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementByDeterminationFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DETERMINATION;

@Service
public class CcjByAdmissionOrDeterminationPdfService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ContentProvider contentProvider;

    @Autowired
    public CcjByAdmissionOrDeterminationPdfService(
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
        String fileBaseName = "";

        if (claim.getCountyCourtJudgment().getCcjType().equals(ADMISSIONS)) {
            fileBaseName = buildRequestForJudgementByAdmissionFileBaseName(claim.getReferenceNumber());
        } else if (claim.getCountyCourtJudgment().getCcjType().equals(DETERMINATION)) {
            fileBaseName = buildRequestForJudgementByDeterminationFileBaseName(claim.getReferenceNumber());
        }

        return new PDF(
            fileBaseName,
            pdfServiceClient.generateFromHtml(
                documentTemplates.getCountyCourtJudgmentByRequest(),
                contentProvider.createContent(claim)),
            ClaimDocumentType.CCJ_REQUEST
        );
    }
}

package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseCopyContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class DefendantResponseCopyService {

    private final DefendantResponseCopyContentProvider contentProvider;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DefendantResponseCopyService(
        final DefendantResponseCopyContentProvider contentProvider,
        final PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(final Claim claim, final byte[] template) {
        requireNonNull(claim);
        return pdfServiceClient.generateFromHtml(template, contentProvider.createContent(claim));
    }
}

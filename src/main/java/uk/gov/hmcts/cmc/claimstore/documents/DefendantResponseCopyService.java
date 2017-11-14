package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseCopyContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class DefendantResponseCopyService {

    private final DefendantResponseCopyContentProvider contentProvider;
    private final EmailContentTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DefendantResponseCopyService(
        DefendantResponseCopyContentProvider contentProvider,
        EmailContentTemplates emailTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        return pdfServiceClient.generateFromHtml(
            emailTemplates.getDefendantResponseCopy(),
            contentProvider.createContent(claim)
        );
    }

}

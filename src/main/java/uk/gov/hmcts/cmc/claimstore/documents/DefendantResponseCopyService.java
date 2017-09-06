package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseCopyContentProvider;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class DefendantResponseCopyService {

    private final DefendantResponseCopyContentProvider contentProvider;
    private final StaffEmailTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DefendantResponseCopyService(
        DefendantResponseCopyContentProvider contentProvider,
        StaffEmailTemplates emailTemplates,
        PDFServiceClient pdfServiceClient) {
        this.contentProvider = contentProvider;
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(Claim claim, DefendantResponse response) {
        requireNonNull(claim);
        requireNonNull(response);
        return pdfServiceClient.generateFromHtml(
            emailTemplates.getDefendantResponseCopy(),
            contentProvider.createContent(claim, response)
        );
    }

}

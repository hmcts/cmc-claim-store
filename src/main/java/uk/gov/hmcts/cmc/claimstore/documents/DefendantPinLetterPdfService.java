package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class DefendantPinLetterPdfService {

    private final EmailContentTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final DefendantPinLetterContentProvider defendantPinLetterContentProvider;

    @Autowired
    public DefendantPinLetterPdfService(
        final EmailContentTemplates emailTemplates,
        final PDFServiceClient pdfServiceClient,
        final DefendantPinLetterContentProvider defendantPinLetterContentProvider
    ) {
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.defendantPinLetterContentProvider = defendantPinLetterContentProvider;
    }

    public byte[] createPdf(final Claim claim, final String defendantPin) {
        requireNonNull(claim);
        requireNonNull(defendantPin);

        return pdfServiceClient.generateFromHtml(
            emailTemplates.getDefendantPinLetter(),
            defendantPinLetterContentProvider.createContent(claim, defendantPin)
        );
    }
}

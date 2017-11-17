package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementPDFContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class SettlementAgreementCopyService {

    private final SettlementAgreementPDFContentProvider contentProvider;
    private final StaffEmailTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public SettlementAgreementCopyService(
        SettlementAgreementPDFContentProvider contentProvider,
        StaffEmailTemplates emailTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        byte[] settlementAgreement = emailTemplates.getSettlementAgreement();
        return pdfServiceClient.generateFromHtml(
            settlementAgreement,
            contentProvider.createContent(claim)
        );
    }

}

package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.bulkprint.BulkPrintEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class BulkPrintStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final BulkPrintEmailContentProvider emailContentProvider;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public BulkPrintStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        BulkPrintEmailContentProvider emailContentProvider,
        PDFServiceClient pdfServiceClient
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.pdfServiceClient = pdfServiceClient;
    }

    public void notifyFailedBulkPrint(Document defendantLetterDocument, Document sealedClaimDocument, Claim claim) {
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));

        EmailAttachment defendantLetter = pdf(
            createPdf(defendantLetterDocument),
            buildDefendantLetterFileBaseName(claim.getReferenceNumber())
        );

        EmailAttachment sealedClaim = pdf(
            createPdf(sealedClaimDocument),
            buildSealedClaimFileBaseName(claim.getReferenceNumber())
        );

        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                ImmutableList.of(defendantLetter, sealedClaim)
            )
        );
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        return map;
    }

    private byte[] createPdf(Document document) {
        requireNonNull(document);

        return pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
    }
}

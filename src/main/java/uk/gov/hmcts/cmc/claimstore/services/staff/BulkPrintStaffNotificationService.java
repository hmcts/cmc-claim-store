package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.content.bulkprint.BulkPrintEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        PDFServiceClient pdfServiceClient) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.pdfServiceClient = pdfServiceClient;
    }

    public void notifyFailedBulkPrint(List<Printable> documents, Claim claim) {
        List<EmailAttachment> emailAttachments = documents.stream()
            .map(d -> pdf(d.getContent(pdfServiceClient), d.getFileName()))
            .collect(Collectors.toList());
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));

        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                emailAttachments
            )
        );
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        return map;
    }
}

package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmccase.utils.PartyUtils;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class CCJStaffNotificationService {

    public static final String FILE_NAME_FORMAT = "%s-%s-county-court-judgment-details.pdf";

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final PDFServiceClient pdfServiceClient;
    private final RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider;
    private final ContentProvider contentProvider;

    @Autowired
    public CCJStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties staffEmailProperties,
        final PDFServiceClient pdfServiceClient,
        final RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider,
        final ContentProvider contentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.pdfServiceClient = pdfServiceClient;
        this.ccjRequestSubmittedEmailContentProvider = ccjRequestSubmittedEmailContentProvider;
        this.contentProvider = contentProvider;
    }

    public void notifyStaffCCJRequestSubmitted(final Claim claim) {
        requireNonNull(claim);
        emailService.sendEmail(staffEmailProperties.getSender(), prepareEmailData(claim));
    }

    private EmailData prepareEmailData(final Claim claim) {
        Map<String, Object> map = createParameterMap(claim);

        EmailContent emailContent = ccjRequestSubmittedEmailContentProvider.createContent(map);
        return new EmailData(
            staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            singletonList(generateCountyCourtJudgmentPdf(claim))
        );
    }

    private Map<String, Object> createParameterMap(final Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("paymentType", claim.getCountyCourtJudgment().getPaymentOption().getDescription());
        return map;
    }

    private EmailAttachment generateCountyCourtJudgmentPdf(final Claim claim) {
        byte[] generatedPdf = pdfServiceClient.generateFromHtml(
            staffEmailProperties.getEmailTemplates().getCountyCourtJudgmentDetails(),
            contentProvider.createContent(claim)
        );

        return pdf(
            generatedPdf,
            format(
                FILE_NAME_FORMAT,
                claim.getClaimData().getDefendant().getName(),
                claim.getReferenceNumber()
            )
        );
    }
}

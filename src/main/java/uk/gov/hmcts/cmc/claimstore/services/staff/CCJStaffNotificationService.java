package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.CCJRequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefaultJudgementContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.PartyUtils;
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

    public static final String FILE_NAME_FORMAT = "%s-default-judgement.pdf";

    private final EmailService emailService;
    private final StaffEmailProperties staffEmailProperties;
    private final PDFServiceClient pdfServiceClient;
    private final CCJRequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider;
    private final DefaultJudgementContentProvider defaultJudgementContentProvider;

    @Autowired
    public CCJStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        PDFServiceClient pdfServiceClient,
        CCJRequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider,
        DefaultJudgementContentProvider defaultJudgementContentProvider
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.pdfServiceClient = pdfServiceClient;
        this.ccjRequestSubmittedEmailContentProvider = ccjRequestSubmittedEmailContentProvider;
        this.defaultJudgementContentProvider = defaultJudgementContentProvider;
    }

    public void notifyStaffCCJRequestSubmitted(Claim claim) {
        requireNonNull(claim);
        emailService.sendEmail(staffEmailProperties.getSender(), prepareEmailData(claim));
    }

    private EmailData prepareEmailData(Claim claim) {
        Map<String, Object> map = createParameterMap(claim);

        EmailContent emailContent = ccjRequestSubmittedEmailContentProvider.createContent(map);
        return new EmailData(
            staffEmailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            singletonList(defaultJudgmentPdf(claim))
        );
    }

    private Map<String, Object> createParameterMap(Claim claim) {
        String requestedDebtRepaymentAction = "immediately";
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("requestedDebtRepaymentAction", requestedDebtRepaymentAction);
        return map;
    }

    private EmailAttachment defaultJudgmentPdf(Claim claim) {
        byte[] generatedPdf = pdfServiceClient.generateFromHtml(
            staffEmailProperties.getEmailTemplates().getLegalSealedClaim(),
            defaultJudgementContentProvider.createContent(claim)
        );

        return pdf(
            generatedPdf,
            format(FILE_NAME_FORMAT, claim.getReferenceNumber())
        );
    }
}

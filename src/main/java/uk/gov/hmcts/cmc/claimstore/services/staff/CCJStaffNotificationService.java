package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.RequestSubmittedNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

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
    private final RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;

    @Autowired
    public CCJStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties staffEmailProperties,
        RequestSubmittedNotificationEmailContentProvider ccjRequestSubmittedEmailContentProvider,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService
    ) {
        this.emailService = emailService;
        this.staffEmailProperties = staffEmailProperties;
        this.ccjRequestSubmittedEmailContentProvider = ccjRequestSubmittedEmailContentProvider;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
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
            singletonList(generateCountyCourtJudgmentPdf(claim))
        );
    }

    private Map<String, Object> createParameterMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("claimReferenceNumber", claim.getReferenceNumber());
        map.put("claimantName", claim.getClaimData().getClaimant().getName());
        map.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        map.put("defendantName", claim.getClaimData().getDefendant().getName());
        map.put("paymentType", claim.getCountyCourtJudgment().getPaymentOption().getDescription());
        return map;
    }

    private EmailAttachment generateCountyCourtJudgmentPdf(Claim claim) {
        byte[] generatedPdf = countyCourtJudgmentPdfService.createPdf(claim);

        return pdf(
            generatedPdf,
            format(
                FILE_NAME_FORMAT,
                claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()
            )
        );
    }
}

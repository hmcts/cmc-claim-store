package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantResponseStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class DefendantResponseStaffNotificationService {

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final DefendantResponseStaffNotificationEmailContentProvider emailContentProvider;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefendantResponseStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        DefendantResponseStaffNotificationEmailContentProvider emailContentProvider,
        DefendantResponseReceiptService defendantResponseReceiptService) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    public void notifyStaffDefenceSubmittedFor(Claim claim, String defendantEmail) {
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim, defendantEmail));
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(createResponsePdfAttachment(claim))
            )
        );
    }

    public static Map<String, Object> wrapInMap(Claim claim, String defendantEmail) {
        Map<String, Object> map = new HashMap<>();
        map.put("claim", claim);
        map.put("response", claim.getResponse().orElseThrow(IllegalStateException::new));
        map.put("defendantEmail", defendantEmail);
        map.put("defendantMobilePhone", claim.getResponse()
            .orElseThrow(IllegalStateException::new)
            .getDefendant()
            .getMobilePhone()
            .orElse(null));
        return map;
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        byte[] defendantResponse = defendantResponseReceiptService.createPdf(claim);
        requireNonNull(defendantResponse);

        return pdf(defendantResponse, buildResponseFileBaseName(claim.getReferenceNumber()) + EXTENSION);
    }
}

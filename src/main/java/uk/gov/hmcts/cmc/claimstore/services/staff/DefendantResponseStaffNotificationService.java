package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantResponseStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service
public class DefendantResponseStaffNotificationService {

    public static final String FILE_NAME_FORMAT = "%s-defendant-response-copy.pdf";

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final DefendantResponseStaffNotificationEmailContentProvider emailContentProvider;
    private final DefendantResponseCopyService defendantResponseCopyService;

    @Autowired
    public DefendantResponseStaffNotificationService(
        final EmailService emailService,
        final StaffEmailProperties emailProperties,
        final DefendantResponseStaffNotificationEmailContentProvider emailContentProvider,
        final DefendantResponseCopyService defendantResponseCopyService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
        this.defendantResponseCopyService = defendantResponseCopyService;
    }

    public void notifyStaffDefenceSubmittedFor(
        Claim claim,
        String defendantEmail
    ) {
        EmailContent emailContent = emailContentProvider.createContent(
            wrapInMap(claim, defendantEmail)
        );
        byte[] defendantResponseCopy = defendantResponseCopyService.createPdf(claim);
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                singletonList(pdf(
                    defendantResponseCopy,
                    format(FILE_NAME_FORMAT, claim.getReferenceNumber())
                ))
            )
        );
    }

    public static Map<String, Object> wrapInMap(
        Claim claim,
        String defendantEmail
    ) {
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

}

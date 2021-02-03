package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.DefenceResponseJsonMapper;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service("rpa/defendant-response-notification-service")
public class DefenceResponseNotificationService {

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final DefenceResponseJsonMapper responseJsonMapper;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefenceResponseNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        DefenceResponseJsonMapper responseJsonMapper,
        DefendantResponseReceiptService defendantResponseReceiptService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.responseJsonMapper = responseJsonMapper;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    @EventListener
    public void notifyRobotics(DefendantResponseEvent event) {
        requireNonNull(event);
        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        requireNonNull(claim);
        DateFormatter.format(claim.getIssuedOn()
            .orElseThrow(() -> new IllegalStateException("Missing issuedOn date")));
        EmailAttachment responsePDFAttachment = createResponsePdfAttachment(claim);

        return new EmailData(emailProperties.getResponseRecipient(),
            "J defence response " + claim.getReferenceNumber(),
            "",
            Lists.newArrayList(responsePDFAttachment, createResponseJsonAttachment(claim))
        );
    }

    private EmailAttachment createResponseJsonAttachment(Claim claim) {
        return EmailAttachment.json(responseJsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

    private EmailAttachment createResponsePdfAttachment(Claim claim) {
        PDF defendantResponse = defendantResponseReceiptService.createPdf(claim);

        return pdf(
            defendantResponse.getBytes(),
            defendantResponse.getFilename());
    }

}

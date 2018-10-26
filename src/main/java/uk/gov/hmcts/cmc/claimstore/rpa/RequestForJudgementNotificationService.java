package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.RequestForJudgementJsonMapper;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.email.EmailAttachment.pdf;

@Service("rpa/request-judgement-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class RequestForJudgementNotificationService {

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final RequestForJudgementJsonMapper jsonMapper;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;

    @Autowired
    public RequestForJudgementNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        RequestForJudgementJsonMapper jsonMapper,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
    }

    @EventListener
    public void notifyRobotics(CountyCourtJudgmentEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        EmailAttachment ccjPdfAttachment = generateCountyCourtJudgmentPdf(claim);

        return new EmailData(emailProperties.getCountyCourtJudgementRecipient(),
            "J default judgement request " + claim.getReferenceNumber(),
            "",
            Lists.newArrayList(ccjPdfAttachment, createRequestForJudgementJsonAttachment(claim))
        );
    }

    private EmailAttachment createRequestForJudgementJsonAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonRequestForJudgementFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

    private EmailAttachment generateCountyCourtJudgmentPdf(Claim claim) {
        byte[] generatedPdf = countyCourtJudgmentPdfService.createPdf(claim);

        return pdf(
            generatedPdf,
            buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()) + EXTENSION
        );
    }

}

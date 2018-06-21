package uk.gov.hmcts.cmc.claimstore.rpa;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.RequestForJudgementJsonMapper;

import javax.validation.constraints.Email;

import static java.util.Objects.requireNonNull;

@Service("rpa/request-judgement-notification-service")
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class RequestForJudgementNotificationService {

    public static final String JSON_EXTENSION = ".json";

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final RequestForJudgementJsonMapper jsonMapper;
    private final CCJStaffNotificationService ccjStaffNotificationService;

    @Autowired
    public RequestForJudgementNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        RequestForJudgementJsonMapper jsonMapper,
        CCJStaffNotificationService ccjStaffNotificationService
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
        this.ccjStaffNotificationService = ccjStaffNotificationService;
    }

    @EventListener
    public void notifyRobotics(CountyCourtJudgmentRequestedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        EmailAttachment ccjPdfAttachment = ccjStaffNotificationService.generateCountyCourtJudgmentPdf(claim);

        return new EmailData(emailProperties.getCountyCourtJudgementRecipient(),
          "J default judgement request " + claim.getReferenceNumber(),
            "",
            Lists.newArrayList(createRequestForJudgementJsonAttachment(claim), ccjPdfAttachment)
        );
    }

    private EmailAttachment createRequestForJudgementJsonAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim).toString().getBytes(),
            DocumentNameUtils.buildJsonRequestForJudgementFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

}

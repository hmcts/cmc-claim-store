package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.rpa.mapper.MoreTimeRequestedJsonMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.services.CaseEventService.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.services.CaseEventService.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.JSON_EXTENSION;

@Service("rpa/more-time-requested-notification-service")
public class MoreTimeRequestedNotificationService {

    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final MoreTimeRequestedJsonMapper jsonMapper;
    private final CaseEventsApi caseEventsApi;
    private final UserService userService;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public MoreTimeRequestedNotificationService(
        EmailService emailService,
        EmailProperties emailProperties,
        MoreTimeRequestedJsonMapper jsonMapper,
        CaseEventsApi caseEventsApi,
        UserService userService,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.jsonMapper = jsonMapper;
        this.caseEventsApi = caseEventsApi;
        this.userService = userService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @EventListener
    public void notifyRobotics(MoreTimeRequestedEvent event) {
        requireNonNull(event);

        EmailData emailData = prepareEmailData(event.getClaim());
        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private EmailData prepareEmailData(Claim claim) {
        return new EmailData(emailProperties.getMoreTimeRequestedRecipient(),
            "J additional time " + claim.getReferenceNumber(),
            "",
            Collections.singletonList(createMoreTimeRequestedAttachment(claim))
        );
    }

    private EmailAttachment createMoreTimeRequestedAttachment(Claim claim) {
        return EmailAttachment.json(jsonMapper.map(claim, getMoreTimeRequested(claim)).toString().getBytes(),
            DocumentNameUtils.buildJsonMoreTimeRequestedFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION);
    }

    private LocalDateTime getMoreTimeRequested(Claim claim) {
        LocalDateTime moreTimeRequestedOnDateTime = LocalDateTime.now();
        User user = userService.authenticateAnonymousCaseWorker();

        List<CaseEventDetail> caseEventDetails = caseEventsApi.findEventDetailsForCase(user.getAuthorisation(),
            authTokenGenerator.generate(), user.getUserDetails().getId(),
            JURISDICTION_ID,
            CASE_TYPE_ID, claim.getCcdCaseId().toString());

        for (CaseEventDetail event : caseEventDetails) {
            if (CaseEvent.MORE_TIME_REQUESTED_ONLINE.getValue().equalsIgnoreCase(event.getEventName())
                || CaseEvent.RESPONSE_MORE_TIME.getValue().equalsIgnoreCase(event.getEventName())) {
                moreTimeRequestedOnDateTime = event.getCreatedDate();
                break;
            }
        }

        return moreTimeRequestedOnDateTime;
    }
}

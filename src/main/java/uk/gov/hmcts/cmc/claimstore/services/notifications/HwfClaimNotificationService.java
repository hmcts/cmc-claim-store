package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.NamedParty;
import uk.gov.hmcts.cmc.domain.models.party.TitledParty;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_INFO_REQUIRED_FOR_HWF;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.ISSUED_ON;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.MORE_INFO_DOCUMENT_LIST;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPOND_TO_CLAIM_URL;

@Service
public class HwfClaimNotificationService {
    private final Logger logger = LoggerFactory.getLogger(HwfClaimNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final AppInsights appInsights;

    @Autowired
    public HwfClaimNotificationService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties,
        AppInsights appInsights
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
        this.appInsights = appInsights;
    }

    @LogExecutionTime
    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(
        Claim claim,
        String targetEmail,
        String emailTemplateId,
        String reference,
        String submitterName
    ) {
        Map<String, String> parameters = aggregateParams(claim, submitterName);
        try {
            notificationClient.sendEmail(emailTemplateId, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        String reference
    ) {
        String errorMessage = String.format(
            "Failure: failed to send notification (%s) due to %s",
            reference,
            exception.getMessage()
        );

        logger.info(errorMessage, exception);
        appInsights.trackEvent(AppInsightsEvent.NOTIFICATION_FAILURE, REFERENCE_NUMBER, reference);

        throw exception;
    }

    private Map<String, String> aggregateParams(Claim claim,
                                                String submitterName) {
        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        if (Boolean.FALSE.equals(claim.getClaimData().isClaimantRepresented())) {
            parameters.put(CLAIMANT_NAME, getNameWithTitle(claim.getClaimData().getClaimant()));
            parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
            parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        } else {
            parameters.put(CLAIMANT_NAME, submitterName);
        }

        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(RESPOND_TO_CLAIM_URL, notificationsProperties.getRespondToClaimUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        if (claim.getLastEventTriggeredForHwfCase() != null
            && claim.getLastEventTriggeredForHwfCase().equals(MORE_INFO_REQUIRED_FOR_HWF.getValue())) {
            parameters.put(MORE_INFO_DOCUMENT_LIST, createHwfMoreInfoDocumentsListFromClaimData(claim));
            Optional<LocalDate> claimIssuedOn = claim.getIssuedOn();
            if (claimIssuedOn.isPresent()) {
                parameters.put(ISSUED_ON, DateFormatter.format(claimIssuedOn.get()));
            }
        }
        return parameters.build();
    }

    private String getNameWithTitle(NamedParty party) {
        StringBuilder title = new StringBuilder();
        if (party instanceof TitledParty) {
            ((TitledParty) party).getTitle().ifPresent(t -> title.append(t).append(" "));
        }

        return title.append(party.getName()).toString();
    }

    private String createHwfMoreInfoDocumentsListFromClaimData(Claim claim) {
        List<String> moreInfoNeededDocumentsList = claim.getClaimData().getHwfMoreInfoNeededDocuments();
        StringBuilder documents = new StringBuilder();
        List<HwFMoreInfoRequiredDocuments> enumList = Arrays.asList(HwFMoreInfoRequiredDocuments.values());

        enumList.forEach(hwFMoreInfoRequiredDocument -> {
            if (moreInfoNeededDocumentsList.contains(hwFMoreInfoRequiredDocument.name())) {
                if (hwFMoreInfoRequiredDocument.name()
                    .equals(HwFMoreInfoRequiredDocuments.ANY_OTHER_INCOME.name())) {
                    documents.append("•\t ").append(moreInfoNeededDocumentsList
                        .get(moreInfoNeededDocumentsList.size() - 1));
                } else {
                    documents.append(hwFMoreInfoRequiredDocument.getDescription());
                }
            }
        });
        return documents.toString();
    }

}

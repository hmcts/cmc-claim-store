package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.ChangeContactDetailsEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CORRESPONDENCE_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CORRESPONDENCE_ADDRESS_REMOVED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EMAIL_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EMAIL_ADDRESS_REMOVED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.MAIN_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PHONE_NUMBER_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PHONE_NUMBER_REMOVED;

@Service
public class ChangeContactDetailsNotificationService {

    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String CORRESPONDENCE_ADDRESS = "CorrespondenceAddress";
    private static final String MAIN_ADDRESS = "MainAddress";
    private static final String EMAIL_ADDRESS = "EmailAddress";
    private static final String PHONE_NUMBER = "PhoneNumber";
    private static final String PARTYNAME = "partyName";
    private static final String OTHERPARTYNAME = "otherPartyName";
    private final  EmailService emailService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;
    private final EmailProperties emailProperties;
    private final ChangeContactDetailsEmailContentProvider changeContactDetailsEmailContentProvider;
    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);

    @Autowired
    public ChangeContactDetailsNotificationService(
            CaseDetailsConverter caseDetailsConverter,
            EmailService emailService,
            NotificationsProperties notificationsProperties,
            EmailProperties emailProperties,
            ChangeContactDetailsEmailContentProvider changeContactDetailsEmailContentProvider) {
        this.emailService = emailService;
        this.notificationsProperties = notificationsProperties;
        this.caseDetailsConverter = caseDetailsConverter;
        this.emailProperties = emailProperties;
        this.changeContactDetailsEmailContentProvider = changeContactDetailsEmailContentProvider;
    }


    public CallbackResponse sendEmailToRightRecipient(CCDCase ccdCase, Claim claim, CCDContactChangeContent contactChangeContent, CCDContactPartyType ccdContactPartyType) {
        boolean errors = false;
        try {

            notifyParty(claim, contactChangeContent, ccdContactPartyType);
        } catch (Exception e) {
            logger.info("Sending email to party failed", e);
            errors = true;
        }
        if (!errors) {
            logger.info("Change Contact Details: Email was sent, case is being updated");
            CCDCase updatedCase = ccdCase.toBuilder()
                .contactChangeParty(null)
                .contactChangeContent(null)
                .generalLetterContent(null)
                .build();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCase))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    private void notifyParty(Claim claim, CCDContactChangeContent contactChangeContent, CCDContactPartyType ccdContactPartyType) {
        EmailContent emailContent = changeContactDetailsEmailContentProvider.createContent(aggregateParams(claim, contactChangeContent, ccdContactPartyType));
//        if(ccdContactPartyType.equals("DEFENDANT")) {
//            emailProperties.setResponseRecipient(claim.getDefendantEmail());
//        } else {
            emailProperties.setResponseRecipient(claim.getDefendantEmail());
            emailProperties.setSender("money.claim@notifications.service.gov.uk");
//        }
        emailService.sendEmail(
                emailProperties.getSender(),e
                new EmailData(
                        emailProperties.getResponseRecipient(),
                        emailContent.getBody(),
                        emailContent.getSubject(),
                        Collections.emptyList()
                )
        );
    }

    private Map<String, Object> aggregateParams(Claim claim, CCDContactChangeContent contactChangeContent, CCDContactPartyType ccdContactPartyType) {
        Map<String, Object> parameters = new HashMap<>();
        if (ccdContactPartyType.equals(CCDContactPartyType.DEFENDANT)) {
            parameters.put(PARTYNAME, claim.getClaimData().getClaimant().getName());
            parameters.put(OTHERPARTYNAME, claim.getClaimData().getDefendant().getName());
        } else {
            parameters.put(OTHERPARTYNAME, claim.getClaimData().getClaimant().getName());
            parameters.put(PARTYNAME, claim.getClaimData().getDefendant().getName());
        }
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        if(contactChangeContent.getCorrespondenceAddress() != null) {
            parameters.put(CORRESPONDENCE_ADDRESS, contactChangeContent.getCorrespondenceAddress());
        }
        if(contactChangeContent.getPrimaryAddress() != null) {
            parameters.put(MAIN_ADDRESS, contactChangeContent.getPrimaryAddress());
        }
        parameters.put(EMAIL_ADDRESS, contactChangeContent.getPrimaryEmail());
        parameters.put(PHONE_NUMBER, contactChangeContent.getTelephone());
        parameters.put(MAIN_ADDRESS_CHANGED, contactChangeContent.getIsPrimaryAddressModified().toBoolean());
        parameters.put(PHONE_NUMBER_CHANGED, contactChangeContent.getIsTelephoneModified().toBoolean());
        parameters.put(EMAIL_ADDRESS_CHANGED, contactChangeContent.getIsEmailModified().toBoolean());
        parameters.put(CORRESPONDENCE_ADDRESS_CHANGED, contactChangeContent.getIsCorrespondenceAddressModified().toBoolean());
        parameters.put(PHONE_NUMBER_REMOVED, contactChangeContent.getTelephoneRemoved().toBoolean());
        parameters.put(CORRESPONDENCE_ADDRESS_REMOVED, contactChangeContent.getCorrespondenceAddressRemoved().toBoolean());
        parameters.put(EMAIL_ADDRESS_REMOVED, contactChangeContent.getPrimaryEmailRemoved().toBoolean());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }
}

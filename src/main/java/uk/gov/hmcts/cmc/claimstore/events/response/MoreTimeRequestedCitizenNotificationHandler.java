package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
public class MoreTimeRequestedCitizenNotificationHandler {

    private final String generalLetterTemplateId;

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final GeneralLetterService generalLetterService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final UserService userService;

    private static final String STANDARD_DEADLINE_TEXT = "You’ve been given an extra 14 days to respond " +
            "to the claim made against you by %s.\n" +
            "You now need to respond to the claim before 4pm on %s.\n" +
            "If you don’t respond, you could get a County Court Judgment (CCJ). " +
            "This may make it harder to get credit, such as a mobile phone contract, credit card or mortgage.";

    @Autowired
    public MoreTimeRequestedCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneralLetterService generalLetterService,
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        UserService userService,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.docAssemblyService = docAssemblyService;
        this.userService = userService;
        this.generalLetterTemplateId = generalLetterTemplateId;
    }

    public CallbackResponse sendNotifications(CallbackParams callbackParams) {
        System.out.println("SEND NOTIFICATIONS");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        sendNotificationToClaimant(claim);
        if (claim.getDefendantEmail() != null && claim.getDefendantId() != null) {
            sendEmailNotificationToDefendant(claim);
        } else {
            return createAndPrintLetter(callbackParams);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendEmailNotificationToDefendant(Claim claim) {
        notificationService.sendMail(
                claim.getDefendantEmail(),
                notificationsProperties.getTemplates().getEmail().getDefendantMoreTimeRequested(),
                prepareNotificationParameters(claim),
                referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private void sendNotificationToClaimant(Claim claim) {
        //sends it 3 times with wrong formatting
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMoreTimeRequested(),
            prepareNotificationParameters(claim),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private CallbackResponse createAndPrintLetter(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
        System.out.println("CREATE AND PRINT LETTER");

        GeneralLetterContent generalLetterContent = GeneralLetterContent.builder()
                .caseworkerName(caseworkerName)
                .letterContent(String.format(STANDARD_DEADLINE_TEXT, claim.getClaimData().getClaimant().getName(),
                        claim.getResponseDeadline()))
                .issueLetterContact(CCDContactPartyType.DEFENDANT)
                .build();
        CCDCase updatedCCDCase = ccdCase.toBuilder()
                .generalLetterContent(generalLetterContent)
                .build();

        docAssemblyService.createGeneralLetter(updatedCCDCase, authorisation, generalLetterTemplateId);
        return generalLetterService.printAndUpdateCaseDocuments(
                callbackParams.getRequest().getCaseDetails(),
                authorisation);
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        return parameters;
    }
}

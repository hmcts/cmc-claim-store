package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
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

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
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
    private final ResponseDeadlineCalculator responseDeadlineCalculator;

    private static final String STANDARD_DEADLINE_TEXT = "You’ve been given an extra 14 days to respond to the"
            + " claim made against you by %s.\n"
            + "\n"
            + "You now need to respond to the claim before 4pm on %s.\n"
            + "\n"
            + "If you don’t respond, you could get a County Court Judgment (CCJ). This may make it harder to get "
            + "credit, such as a mobile phone contract, credit card or mortgage.";

    @Autowired
    public MoreTimeRequestedCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneralLetterService generalLetterService,
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        UserService userService,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.docAssemblyService = docAssemblyService;
        this.userService = userService;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.generalLetterTemplateId = generalLetterTemplateId;
    }

    public CallbackResponse sendNotifications(CallbackParams callbackParams) throws URISyntaxException {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        LocalDate responseDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());
        CallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
        if (claim.getDefendantEmail() != null && claim.getDefendantId() != null) {
            sendEmailNotificationToDefendant(claim, responseDeadline);
        } else {
            response = createAndPrintLetter(callbackParams);
        }
        sendNotificationToClaimant(claim, responseDeadline);
        return response;
    }

    private void sendEmailNotificationToDefendant(Claim claim, LocalDate deadline) {
        notificationService.sendMail(
                claim.getDefendantEmail(),
                notificationsProperties.getTemplates().getEmail().getDefendantMoreTimeRequested(),
                prepareNotificationParameters(claim,deadline),
                referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private void sendNotificationToClaimant(Claim claim, LocalDate deadline) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMoreTimeRequested(),
            prepareNotificationParameters(claim, deadline),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private CallbackResponse createAndPrintLetter(CallbackParams callbackParams) throws URISyntaxException {

            String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            UserDetails userDetails = userService.getUserDetails(authorisation);
            String caseworkerName = userDetails.getFullName();
            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
            Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());
            LocalDate responseDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

            GeneralLetterContent generalLetterContent = GeneralLetterContent.builder()
                    .caseworkerName(caseworkerName)
                    .letterContent(String.format(STANDARD_DEADLINE_TEXT, claim.getClaimData().getClaimant().getName(),
                            formatDate(responseDeadline)))
                    .issueLetterContact(CCDContactPartyType.DEFENDANT)
                    .build();

            CCDCase updatedCCDCase = ccdCase.toBuilder()
                    .generalLetterContent(generalLetterContent)
                    .build();

            CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue().toBuilder()
                    .responseMoreTimeNeededOption(CCDYesNoOption.YES)
                    .responseDeadline(responseDeadline)
                    .build();

            DocAssemblyResponse docAssemblyResponse = docAssemblyService
                .createGeneralLetter(updatedCCDCase, authorisation, generalLetterTemplateId);
            CCDDocument ccdDocument = CCDDocument.builder()
                .documentUrl(docAssemblyResponse.getRenditionOutputLocation())
                .documentBinaryUrl(docAssemblyResponse.getRenditionOutputLocation() + "/binary")
                .documentFileName("")
                .build();
            updatedCCDCase.setDraftLetterDoc(ccdDocument);
            generalLetterService.printLetter(authorisation, updatedCCDCase.getDraftLetterDoc(), claim);
            CCDCase updatedCase = ccdCase.toBuilder()
                    .caseDocuments(generalLetterService
                    .updateCaseDocumentsWithGeneralLetter(
                            updatedCCDCase,
                            updatedCCDCase.getDraftLetterDoc(),
                            String.format("%s-response-deadline-extended.pdf",
                                    claim.getReferenceNumber())))
                    .generalLetterContent(null)
                    .draftLetterDoc(null)
                    .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                            .value(respondent)
                            .build()))
                    .build();

            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCase))
                .build();
//        } catch (Exception e) {
//            e.getStackTrace();
//            return AboutToStartOrSubmitCallbackResponse
//                .builder()
//                .errors(Collections.singletonList(ERROR_MESSAGE))
//                .build();
//        }
    }

    private Map<String, String> prepareNotificationParameters(Claim claim, LocalDate deadline) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(RESPONSE_DEADLINE, formatDate(deadline));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        return parameters;
    }
}

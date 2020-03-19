package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);
    private static final String NO_DETAILS_CHANGED_ERROR = "Notifications cannot be send if contact details we not changed.";
    private static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String BODY = "body";

    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    private final LetterContentBuilder letterContentBuilder;

    public ChangeContactDetailsPostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        ChangeContactDetailsNotificationService changeContactDetailsNotificationService,
        LetterContentBuilder letterContentBuilder
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
        this.letterContentBuilder = letterContentBuilder;
    }


    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");

        Map<String, Object> data = new HashMap<>();

        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claimBefore = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
        Claim claimNow = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());

        data.put("contactChanges", letterContentBuilder.letterContent(claimBefore, claimNow));

        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(data)
                .build();
    }


//    public CallbackResponse notifyPartiesViaEmailAndLetter(CallbackParams callbackParams) {
//        logger.info("Change Contact Details: print letter or send email notifications");
//        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
//        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
//        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
//        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
//
////        return letterNeeded(ccdCase, claim)
////                ? printDefendantLetter()
////                : changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);
//
//    }

    public void printDefendantLetter() {
        //send to bulkprint
        //delete function if too short
    }

}




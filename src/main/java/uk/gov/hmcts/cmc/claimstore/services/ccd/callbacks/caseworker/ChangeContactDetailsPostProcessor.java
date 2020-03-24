package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);
    private static final String NO_DETAILS_CHANGED_ERROR = "Notifications cannot be send if contact details we not changed.";
    private static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String BODY = "body";

    private final CaseDetailsConverter caseDetailsConverter;
    private final LetterGeneratorService letterGeneratorService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    private final LetterContentBuilder letterContentBuilder;
    private final UserService userService;

    public ChangeContactDetailsPostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        LetterGeneratorService letterGeneratorService,
        ChangeContactDetailsNotificationService changeContactDetailsNotificationService,
        LetterContentBuilder letterContentBuilder,
        UserService userService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.letterGeneratorService = letterGeneratorService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
        this.letterContentBuilder = letterContentBuilder;
        this.userService = userService;
    }


    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");

        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase caseBefore = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetailsBefore());
        CCDCase caseNow = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        CCDParty partyBefore = getPartyDetail(caseBefore);
        CCDParty partyNow = getPartyDetail(caseNow);

        CCDContactChangeContent contactChangeContent = letterContentBuilder.letterContent(partyBefore, partyNow);
        CCDCase input = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();
        CCDCase ccdCase = input.toBuilder()
            .contactChangeContent(contactChangeContent.toBuilder()
                .caseworkerName(caseworkerName)
                .build())
            .build();

        DocAssemblyResponse generalLetter = letterGeneratorService.createGeneralLetter(ccdCase, authorisation);

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                "contactChangeContent",
                contactChangeContent,
                DRAFT_LETTER_DOC,
                CCDDocument.builder().documentUrl(generalLetter.getRenditionOutputLocation()).build()
            ))
            .build();
    }

    private CCDParty getPartyDetail(CCDCase ccdCase) {
        if (ccdCase.getContactChangeParty() == CCDContactPartyType.CLAIMANT) {
            return ccdCase.getApplicants().get(0).getValue().getPartyDetail();
        } else {
            return ccdCase.getRespondents().get(0).getValue().getPartyDetail();
        }
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




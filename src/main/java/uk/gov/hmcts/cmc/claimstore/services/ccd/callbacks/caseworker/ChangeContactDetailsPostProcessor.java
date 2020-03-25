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
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import java.util.Collections;


@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);
    private static final String NO_DETAILS_CHANGED_ERROR = "Notifications cannot be send if contact details we not changed.";
    private static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";

    private final CaseDetailsConverter caseDetailsConverter;
    private final LetterGeneratorService letterGeneratorService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    private final LetterContentBuilder letterContentBuilder;
    private final UserService userService;
    private final GeneralLetterService generalLetterService;

    public ChangeContactDetailsPostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        LetterGeneratorService letterGeneratorService,
        ChangeContactDetailsNotificationService changeContactDetailsNotificationService,
        LetterContentBuilder letterContentBuilder,
        UserService userService,
        GeneralLetterService generalLetterService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.letterGeneratorService = letterGeneratorService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
        this.letterContentBuilder = letterContentBuilder;
        this.userService = userService;
        this.generalLetterService = generalLetterService;
    }


    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");

        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase caseBefore = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetailsBefore());
        CCDCase caseNow = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        CCDContactPartyType contactChangeParty = caseNow.getContactChangeParty();
        CCDParty partyBefore = getPartyDetail(caseBefore, contactChangeParty);
        CCDParty partyNow = getPartyDetail(caseNow, contactChangeParty);

        CCDContactChangeContent contactChangeContent = letterContentBuilder.letterContent(partyBefore, partyNow);

        if(contactChangeContent.noContentChange()){
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList("Please change some content."))
                .build();
        }

        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CCDCase ccdCase = caseNow.toBuilder()
            .contactChangeContent(contactChangeContent.toBuilder()
                .caseworkerName(getCaseworkerName(authorisation))
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

    private String getCaseworkerName(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    private CCDParty getPartyDetail(CCDCase ccdCase, CCDContactPartyType contactPartyType) {
        if (contactPartyType == CCDContactPartyType.CLAIMANT) {
            return ccdCase.getApplicants().get(0).getValue().getPartyDetail();
        } else {
            return ccdCase.getRespondents().get(0).getValue().getPartyDetail();
        }
    }

    public CallbackResponse notifyPartiesViaEmailAndLetter(CallbackParams callbackParams) {
        logger.info("Change Contact Details: print letter or send email notifications");
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CCDContactChangeContent contactChangeContent = ccdCase.getContactChangeContent();

        return letterNeeded(ccdCase, claim)
                ? generalLetterService.printAndUpdateCaseDocuments(caseDetails, authorisation)
                : changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim, contactChangeContent);
    }

    public boolean letterNeeded(CCDCase ccdCase, Claim claim) {
        return ccdCase.getContactChangeParty() == CCDContactPartyType.CLAIMANT
                && (claim.getDefendantId().isEmpty() || claim.getDefendantId() == null);
    }

}




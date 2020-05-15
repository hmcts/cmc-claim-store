package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType.CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ChangeContactDetailsPostProcessor.class);
    public static final String NO_DETAILS_CHANGED_ERROR = "You need to change contact details to continue.";
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";

    private final CaseDetailsConverter caseDetailsConverter;
    private final ChangeContactLetterService changeContactLetterService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    private final LetterContentBuilder letterContentBuilder;
    private final UserService userService;

    public ChangeContactDetailsPostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        ChangeContactLetterService changeContactLetterService,
        ChangeContactDetailsNotificationService changeContactDetailsNotificationService,
        LetterContentBuilder letterContentBuilder,
        UserService userService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.changeContactLetterService = changeContactLetterService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
        this.letterContentBuilder = letterContentBuilder;
        this.userService = userService;
    }

    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");

        CallbackRequest callbackRequest = callbackParams.getRequest();
        var callbackResponse = AboutToStartOrSubmitCallbackResponse.builder();
        CCDCase caseBefore = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetailsBefore());
        CCDCase caseNow = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        CCDContactPartyType contactChangeParty = caseNow.getContactChangeParty();
        CCDParty partyBefore = getPartyDetail(caseBefore, contactChangeParty);
        CCDParty partyNow = getPartyDetail(caseNow, contactChangeParty);

        CCDContactChangeContent contactChangeContent = letterContentBuilder.letterContent(partyBefore, partyNow);

        if (contactChangeContent.noContentChange()) {
            return callbackResponse
                .errors(Collections.singletonList(NO_DETAILS_CHANGED_ERROR))
                .build();
        }

        ImmutableMap.Builder<String, Object> data = ImmutableMap.<String, Object>builder()
            .put("contactChangeContent", contactChangeContent);

        if (letterNeededForDefendant(contactChangeParty, caseBefore)) {
            String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            CCDCase ccdCase = caseNow.toBuilder()
                .contactChangeContent(contactChangeContent.toBuilder()
                    .letterNeededForDefendant(CCDYesNoOption.YES)
                    .caseworkerName(getCaseworkerName(authorisation))
                    .build())
                .respondents(caseBefore.getRespondents())
                .build();

            data.put(DRAFT_LETTER_DOC, CCDDocument.builder()
                .documentUrl(changeContactLetterService.createGeneralLetter(ccdCase, authorisation))
                .build());
        }

        return callbackResponse.data(data.build()).build();
    }

    private String getCaseworkerName(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        return userDetails.getFullName();
    }

    private CCDParty getPartyDetail(CCDCase ccdCase, CCDContactPartyType contactPartyType) {
        if (contactPartyType == CLAIMANT) {
            return ccdCase.getApplicants().get(0).getValue().getPartyDetail();
        } else {
            return ccdCase.getRespondents().get(0).getValue().getPartyDetail();
        }
    }

    public CallbackResponse performPostProcesses(CallbackParams callbackParams) {
        logger.info("Change Contact Details: print letter or send email notifications");
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        CCDCase updatedCase = ccdCase;
        try {
            if (letterNeededForDefendant(ccdCase.getContactChangeParty(), updatedCase)) {
                updatedCase = changeContactLetterService.publishLetter(updatedCase, claim, authorisation);
                logger.info("Change Contact Details: Letter is sent to defendant");
            } else {
                changeContactDetailsNotificationService.sendEmailToRightRecipient(updatedCase, claim);
                updatedCase = updatedCase.toBuilder().contactChangeParty(null).contactChangeContent(null).build();
                logger.info("Change Contact Details: Email was sent to the party");
            }

            return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
        } catch (Exception e) {
            logger.error("Error performing post processing", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
    }

    public boolean letterNeededForDefendant(CCDContactPartyType contactPartyType, CCDCase ccdCase) {
        CCDRespondent ccdRespondent = ccdCase.getRespondents().get(0).getValue();
        return contactPartyType == CLAIMANT && StringUtils.isBlank(ccdRespondent.getDefendantId());
    }
}

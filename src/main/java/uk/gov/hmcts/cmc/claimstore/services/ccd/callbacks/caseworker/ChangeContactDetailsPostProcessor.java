package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

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
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType.CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;

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
    private final CCDCaseApi ccdCaseApi;

    public ChangeContactDetailsPostProcessor(
        CaseDetailsConverter caseDetailsConverter,
        ChangeContactLetterService changeContactLetterService,
        ChangeContactDetailsNotificationService changeContactDetailsNotificationService,
        LetterContentBuilder letterContentBuilder,
        UserService userService,
        CCDCaseApi ccdCaseApi
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.changeContactLetterService = changeContactLetterService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
        this.letterContentBuilder = letterContentBuilder;
        this.userService = userService;
        this.ccdCaseApi = ccdCaseApi;
    }

    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CallbackRequest callbackRequest = callbackParams.getRequest();
        var callbackResponse = AboutToStartOrSubmitCallbackResponse.builder();
        CCDCase caseNow = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        CCDCase caseBefore = caseDetailsConverter
            .convertTo(ccdCaseApi.getByExternalId(caseNow.getExternalId(), authorisation)
                .orElseThrow(() -> new NotFoundException(format("Claim %s does not exist", caseNow.getExternalId()))));
        CCDContactPartyType contactChangeParty = caseNow.getContactChangeParty();
        CCDParty partyBefore = getPartyDetail(caseBefore, contactChangeParty);
        CCDParty partyNow = getPartyDetail(caseNow, contactChangeParty);

        CCDContactChangeContent contactChangeContent = letterContentBuilder.letterContent(partyBefore, partyNow);

        if (contactChangeContent.noContentChange()) {
            return callbackResponse
                .errors(Collections.singletonList(NO_DETAILS_CHANGED_ERROR))
                .build();
        }

        return callbackResponse
            .data(Map.of("contactChangeContent", contactChangeContent))
            .build();
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

                updatedCase = updatedCase.toBuilder()
                    .contactChangeContent(updatedCase.getContactChangeContent().toBuilder()
                        .letterNeededForDefendant(CCDYesNoOption.YES)
                        .caseworkerName(getCaseworkerName(authorisation))
                        .build())
                    .respondents(updatedCase.getRespondents())
                    .build();

                String generalLetter = changeContactLetterService.createGeneralLetter(updatedCase, authorisation);

                CCDDocument letterDoc = CCDDocument.builder().documentUrl(generalLetter)
                    .documentFileName(buildLetterFileBaseName(updatedCase.getPreviousServiceCaseReference(),
                        String.valueOf(LocalDate.now())))
                    .build();

                updatedCase = changeContactLetterService.publishLetter(updatedCase, claim, authorisation, letterDoc);
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

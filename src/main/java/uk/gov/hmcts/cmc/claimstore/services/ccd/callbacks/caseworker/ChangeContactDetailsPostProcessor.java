package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.ContactPartyType;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);
    private static final String NO_DETAILS_CHANGED_ERROR = "Notifications cannot be send if contact details we not changed.";
    private static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String BODY = "body";
    private static final String FIRST_LINE = "We’re contacting you because ((ClaimantName)) has changed their contact details.";
    private static final String CLAIMANT = "CLAIMANT";

    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    Boolean address;
    Boolean phone;
    Boolean email;
    Boolean corraddress;
    Boolean phoneRemoved;
    Boolean corrAddressRemoved;
    Boolean emailRemoved;


    public ChangeContactDetailsPostProcessor(CaseDetailsConverter caseDetailsConverter,
                                             DocAssemblyService docAssemblyService,
                                             ChangeContactDetailsNotificationService changeContactDetailsNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
    }


    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) {
        logger.info("Change Contact Details: create letter (preview)");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        compareClaims(callbackRequest);

//        if (letterNeeded(ccdCase, claim)) {
        return defendantLetter(callbackParams);
//        }
//

        // return email template
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

    public void compareClaims(CallbackRequest callbackRequest) {
        Claim claimBefore = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
        Claim claimNow = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());

        Address oldAddress = claimBefore.getClaimData().getClaimant().getAddress();
        Address newAddress = claimNow.getClaimData().getClaimant().getAddress();

        Optional<Address> oldCorrespondenceAddress = claimBefore.getClaimData().getClaimant().getCorrespondenceAddress();
        Optional<Address> newCorrespondenceAddress = claimNow.getClaimData().getClaimant().getCorrespondenceAddress();

        Optional<String> oldPhone = claimBefore.getClaimData().getClaimant().getPhone();
        Optional<String> newPhone = claimNow.getClaimData().getClaimant().getPhone();

        String oldEmail = claimBefore.getSubmitterEmail();
        String newEmail = claimNow.getSubmitterEmail();

        if (!oldAddress.equals(newAddress)) {
            address = true;
        }
        if (!oldCorrespondenceAddress.equals(newCorrespondenceAddress)) {
            corraddress = true;
            if (!newCorrespondenceAddress.isPresent()) {
                corrAddressRemoved = true;
            }
        }
        if (!oldPhone.equals(newPhone)) {
            phone = true;
            if (!newPhone.isPresent()) {
                phoneRemoved = true;
            }
        }
        if (!oldEmail.equals(newEmail)) {
            email = true;
            if (newEmail.isEmpty()) {
                emailRemoved = true;
            }
        }
    }

    public CallbackResponse defendantLetter(CallbackParams callbackParams) {
        AboutToStartOrSubmitCallbackResponse response;
        Map<String, Object> data = new HashMap<>();
        String partyType = callbackParams
            .getRequest().getCaseDetails()
            .getData().get(CHANGE_CONTACT_PARTY).toString();

        String body = "We’re contacting you because ((ClaimantName)) has changed their contact details.";
        if (address) {
            body += "Their address is now: ((Claimant address))";
        }
        if (phone) {
            body += "Their phone number is now: ((Claimant phone))";
        }
        if (corraddress) {
            body += "The address they want to use for post about the claim is now:: ((Claimant correspondence addr))";
        }
        if (email) {
            body += "Their email address is now:: ((Claimant email))";
        }
        if (phoneRemoved) {
            body += "They’ve removed their phone number.";
        }
        if (corrAddressRemoved) {
            body += "They’ve removed the address they want to use for post about the claim.";
        }
        if (emailRemoved) {
            body += "They’ve removed their email address.";
        }

        if (!body.equals(FIRST_LINE)) {
            data.put(BODY, body);
            data.put(CHANGE_CONTACT_PARTY, partyType);
            data.put("changeContactPreview", body);
//            DocAssemblyResponse docAssemblyResponse = docAssemblyService
//                    .createGeneralLetter(ccdCase, authorisation, data);
            response = AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(data)
                .build();
        } else {
            response = AboutToStartOrSubmitCallbackResponse.builder()
                .errors(Collections.singletonList(NO_DETAILS_CHANGED_ERROR)).build();
        }
        return response;
    }

    public boolean letterNeeded(CCDCase ccdCase, Claim claim) {
        return ccdCase.getChangeContactParty() == ContactPartyType.CLAIMANT
            && (claim.getDefendantId().isEmpty() || claim.getDefendantId() == null);
    }
}




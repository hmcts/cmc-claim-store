package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {
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
        logger.info("New Contact Details: creating preview");

        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        compareClaims(callbackRequest);



        Map<String, Object> data = new HashMap<>();
        String body = callbackParams
                .getRequest().getCaseDetails()
                .getData().get(LETTER_CONTENT).toString();
        String partyType = callbackParams
                .getRequest().getCaseDetails()
                .getData().get(CHANGE_CONTACT_PARTY).toString();
        if (body != null) {
            data.put(BODY, body);
            data.put(CHANGE_CONTACT_PARTY, partyType);
            String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            DocAssemblyResponse docAssemblyResponse = docAssemblyService
                    .createGeneralLetter(ccdCase, authorisation, data);
            response = AboutToStartOrSubmitCallbackResponse
                    .builder()
                    .data(ImmutableMap.of(
                            DRAFT_LETTER_DOC,
                            CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
                    ))
                    .build();
        } else {
            response = AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(Collections.singletonList(EMPTY_BODY_ERROR)).build();
        }
        return response;
    }


    //how do I persist the boolean values of what has changed for the email template

        //what am I sending back?


//    public CallbackResponse notifyPartiesViaEmailAndLetter(CallbackParams callbackParams) {
//        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
//        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
//        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
//        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
//
//
//
//        return claim.getDefendantId().isEmpty() || claim.getDefendantId() == null
//                ? sendDefendantLetter() //send letter to bulkprint
//                : changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);
//
//    }

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
        if (!oldCorrespondenceAddress.equals(newCorrespondenceAddress)){
            corraddress = true;
            if (!newCorrespondenceAddress.isPresent()){
                corrAddressRemoved = true;
            }
        }
        if (!oldPhone.equals(newPhone)){
            phone = true;
            if (!newPhone.isPresent()){
                phoneRemoved = true;
            }
        }
        if (!oldEmail.equals(newEmail)){
            email = true;
            if (newEmail.isEmpty()){
                emailRemoved = true;
            }
        }
    }
}




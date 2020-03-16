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


    public CallbackResponse showNewContactDetails(CallbackParams callbackParams) throws IOException {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
//
//        ObjectMapper mapper = new ObjectMapper();
//        TypeReference<Map<String, Object>> type =
//                new TypeReference<Map<String, Object>>() {};

//        Map<String, Object> leftMap = mapper.readValue((JsonParser) callbackRequest.getCaseDetailsBefore().getData(), type);
//        Map<String, Object> rightMap = mapper.readValue((JsonParser) callbackRequest.getCaseDetails().getData(), type);
//
//        MapDifference<String, Object> difference = Maps.difference(leftMap, rightMap);
//
//        Map<String, Object> result = Collections.emptyMap();
//
//        difference.entriesDiffering()
//                .forEach(result::put);
//        System.out.println();
//
//        if (result.containsKey("address")) {
//            address = true;
//        }
//        if (result.containsKey("correspondenceAddress")){
//            corraddress = true;
//            if (result.get("correspondenceAddress") == null){
//                corrAddressRemoved = true;
//            }
//        }
//        if (result.containsKey("phoneNumber")){
//            phone = true;
//            if (result.get("phoneNumber") == null){
//                phoneRemoved = true;
//            }
//        }
//        if (result.containsKey("emailAddress")){
//            email = true;
//            if (result.get("emailAddress") == null){
//                emailRemoved = true;
//            }
//        }

        Claim claimBefore = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
        Claim claimNow = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());

        Address oldAddress = claimBefore.getClaimData().getClaimant().getAddress();
        Address newAddress = claimNow.getClaimData().getClaimant().getAddress();

        Optional<Address> oldCorrespondenceAddress = claimBefore.getClaimData().getClaimant().getCorrespondenceAddress();
        Optional<Address> newCorrespondenceAddress = claimNow.getClaimData().getClaimant().getCorrespondenceAddress();

        String oldPhone = claimBefore.getClaimData().getClaimant().getPhone().toString();
        String newPhone = claimNow.getClaimData().getClaimant().getPhone().toString();

        String oldEmail = claimBefore.getSubmitterEmail();
        String newEmail = claimNow.getSubmitterEmail();

//        , claimBefore.getSubmitterEmail(), oldPhone
//                , claimNow.getSubmitterEmail(), newPhone

        List<String> old = Arrays.asList(oldAddress.getLine1(), oldAddress.getLine2(), oldAddress.getLine3(), oldAddress.getCity(), oldAddress.getCounty(), oldAddress.getPostcode());
        List<String> newlist = Arrays.asList(newAddress.getLine1(), newAddress.getLine2(), newAddress.getLine3(), newAddress.getCity(), newAddress.getCounty(), newAddress.getPostcode());

        List<String> result = newlist.stream().filter(aObject ->
                !old.contains(aObject)).collect(Collectors.toList());


        System.out.println(result);


        //how do I persist the boolean values of what has changed for the email template

        //what am I sending back?


        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(result)
                .build();
    }

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

//    public void sendDefendantLetter() {
//        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createDefendantLetter(ccdCase, authorisation);
//        //print somehow via bulkprint
//    }
}




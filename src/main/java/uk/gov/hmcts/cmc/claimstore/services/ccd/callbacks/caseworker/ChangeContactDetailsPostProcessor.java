package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.ChangeContactDetailsRule;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class ChangeContactDetailsPostProcessor {
    private final CaseDetailsConverter caseDetailsConverter;
    private final ChangeContactDetailsRule changeContactDetailsRule;
    private final DocAssemblyService docAssemblyService;
    private final ChangeContactDetailsNotificationService changeContactDetailsNotificationService;

    public ChangeContactDetailsPostProcessor(CaseDetailsConverter caseDetailsConverter,
                                             ChangeContactDetailsRule changeContactDetailsRule,
                                             DocAssemblyService docAssemblyService,
                                             ChangeContactDetailsNotificationService changeContactDetailsNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.changeContactDetailsRule = changeContactDetailsRule;
        this.docAssemblyService = docAssemblyService;
        this.changeContactDetailsNotificationService = changeContactDetailsNotificationService;
    }


    public CallbackResponse notifyPartiesViaEmailAndLetter(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

        return claim.getDefendantId().isEmpty() || claim.getDefendantId() == null
                ? printOrder(authorisation, claim, ccdCase) //send letter to bulkprint
                : changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);
    }

    public CallbackResponse generateNotificationContent(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim oldClaim = caseDetailsConverter.extractClaim(caseDetails);
        //immutable list of changeable details

        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(oldClaim)
                .build();
    }

    public CallbackResponse generateNotificationContent(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        List<String> validations = changeContactDetailsRule.validateExpectedFieldsFilledByCaseworker(ccdCase);
        if (!validations.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validations).build();
        }

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseDetails caseDetailsNow = callbackRequest.getCaseDetails();
        System.out.println(caseDetailsBefore.getData().keySet());
        List oldContactDetails = ImmutableList.of();
        List newContactDetails = ImmutableList.of();


        //get changed values to pass to email and template

        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(ImmutableMap.of(
                        DRAFT_ORDER_DOC,
                        CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
                ))
                .build();
    }

    private buildDefendantLetter() {
        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createDefendantLetter(ccdCase, authorisation);

    }

}




package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GeneralLetterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String LETTER_CONTENT = "letterContent";
    private static final String EMPTY_BODY_ERROR = "The body of the letter cannot be empty";
    private static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String BODY = "body";

    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;

    public GeneralLetterService(CaseDetailsConverter caseDetailsConverter,
                                DocAssemblyService docAssemblyService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
    }

    public CallbackResponse createAndPreview(CallbackParams callbackParams) {
        logger.info("General Letter: creating letter");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        AboutToStartOrSubmitCallbackResponse response;
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

    public CallbackResponse sendToPrint(CallbackParams callbackParams) {
        logger.info("General Letter creator: sending to print");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        return null;
    }
}

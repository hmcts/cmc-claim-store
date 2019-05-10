package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DrawOrderCallbackHandler extends CallbackHandler {
    private final Clock clock;
    private final JsonMapper jsonMapper;

    @Autowired
    public DrawOrderCallbackHandler(
        Clock clock, JsonMapper jsonMapper) {
        this.clock = clock;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::copyDraftToCaseDocument
        );
    }

    private CallbackResponse copyDraftToCaseDocument(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = jsonMapper.fromMap(
            callbackRequest.getCaseDetails().getData(), CCDCase.class);

        CCDDocument draftOrderDoc = Optional.ofNullable(ccdCase.getOrderGenerationData())
            .map(CCDOrderGenerationData::getDraftOrderDoc)
            .orElseThrow(() -> new CallbackException("Draft order not present"));

        CCDCollectionElement<CCDClaimDocument> claimDocument =
            CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(draftOrderDoc)
                    .createdDatetime(LocalDateTime.now(clock))
                    .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                    .build())
                .build();

        List<CCDCollectionElement<CCDClaimDocument>> currentCaseDocuments =
            Optional.ofNullable(ccdCase.getCaseDocuments())
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        currentCaseDocuments.add(claimDocument);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                "caseDocuments", currentCaseDocuments))
            .build();
    }
}

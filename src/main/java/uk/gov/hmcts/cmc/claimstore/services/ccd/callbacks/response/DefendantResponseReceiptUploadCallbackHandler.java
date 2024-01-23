package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_RESPONSE_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class DefendantResponseReceiptUploadCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Arrays.asList(DEFENDANT_RESPONSE_UPLOAD);
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private final CaseDetailsConverter caseDetailsConverter;
    private final DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    public DefendantResponseReceiptUploadCallbackHandler(CaseDetailsConverter caseDetailsConverter, DefendantResponseReceiptService defendantResponseReceiptService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_SUBMIT, this::uploadDefendantResponseReceipt
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse uploadDefendantResponseReceipt(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails())
            .toBuilder().lastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId()).build();
        if (!hasDocumentAttached(claim)) {
            defendantResponseReceiptService.createPdf(claim);
        }
        return
            AboutToStartOrSubmitCallbackResponse
            .builder()
            .build();
    }

    private boolean hasDocumentAttached(Claim claim) {
        return claim.getClaimDocumentCollection()
            .flatMap(claimDocumentCollection ->
                claimDocumentCollection
                    .getDocument(
                        ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT))
                        .isPresent();
    }
}

package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_PART_ADMIT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_STATES_PAID;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MANAGE_DOCUMENTS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class ManageDocumentsCallbackHandler  extends CallbackHandler {
    static final String NO_CHANGES_ERROR_MESSAGE = "You need to upload, edit or delete a document to continue.";
    static final String PAPER_RESPONSE_ERROR_MESSAGE = "Error as uploaded document type is paper response";

    private static final List<Role> ROLES = ImmutableList.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MANAGE_DOCUMENTS);

    private static final List<CCDClaimDocumentType> paperResponseDocumentTypes
        = ImmutableList.of(PAPER_RESPONSE_FULL_ADMIT, PAPER_RESPONSE_PART_ADMIT, PAPER_RESPONSE_STATES_PAID,
        PAPER_RESPONSE_MORE_TIME, PAPER_RESPONSE_DISPUTES_ALL);

    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.MID, this::checkList
        );
    }

    ManageDocumentsCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    private CallbackResponse checkList(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();

        List<String> errors = new ArrayList<>();

        if (paperResponseSelected(caseDetailsConverter.extractCCDCase(request.getCaseDetails()))) {
            errors.add(PAPER_RESPONSE_ERROR_MESSAGE);
        }

        if (request.getCaseDetails().equals(request.getCaseDetailsBefore())) {
            errors.add(NO_CHANGES_ERROR_MESSAGE);
        }

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder builder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (!errors.isEmpty()) {
            builder.errors(errors);
        }
        return builder.build();
    }

    private boolean paperResponseSelected(CCDCase ccdCase) {
        List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocuments = ccdCase.getStaffUploadedDocuments();
        return staffUploadedDocuments != null && staffUploadedDocuments
            .stream()
            .map(d -> d.getValue().getDocumentType())
            .anyMatch(paperResponseDocumentTypes::contains);
    }
}

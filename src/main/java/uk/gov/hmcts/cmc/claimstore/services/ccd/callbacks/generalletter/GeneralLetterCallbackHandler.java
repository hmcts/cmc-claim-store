package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty({"doc_assembly.url", "feature_toggles.ctsc_enabled"})
public class GeneralLetterCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(ISSUE_GENERAL_LETTER);
    private final GeneralLetterService generalLetterService;
    protected static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private final String generalLetterTemplateId;

    @Autowired
    public GeneralLetterCallbackHandler(
        GeneralLetterService generalLetterService,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId
    ) {
        this.generalLetterService = generalLetterService;
        this.generalLetterTemplateId = generalLetterTemplateId;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::prepopulateData,
            CallbackType.MID, this::createAndPreview,
            CallbackType.ABOUT_TO_SUBMIT, this::printAndUpdateCaseDocuments
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

    private CallbackResponse prepopulateData(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        return generalLetterService.prepopulateData(authorisation);
    }

    public CallbackResponse createAndPreview(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        return generalLetterService.createAndPreview(
            callbackParams.getRequest().getCaseDetails(),
            authorisation,
            DRAFT_LETTER_DOC,
            generalLetterTemplateId);
    }

    public CallbackResponse printAndUpdateCaseDocuments(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        return generalLetterService.printAndUpdateCaseDocuments(
            callbackParams.getRequest().getCaseDetails(),
            authorisation);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.bulkscan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod;
import uk.gov.hmcts.cmc.ccd.util.MapperUtil;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ATTACH_SCANNED_DOCS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;

@Service
public class AttachScannedDocsCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(ATTACH_SCANNED_DOCS);

    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public AttachScannedDocsCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(ABOUT_TO_SUBMIT, this::setDefendantResponseMethod);
    }

    private AboutToStartOrSubmitCallbackResponse setDefendantResponseMethod(CallbackParams callbackParams) {
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());

        if (MapperUtil.hasPaperResponse.apply(ccdCase) == YesNoOption.YES) {
            CCDCollectionElement<CCDRespondent> originalElement = ccdCase.getRespondents().get(0);

            CCDRespondent respondent =
                originalElement.getValue()
                    .toBuilder()
                    .responseMethod(CCDResponseMethod.OFFLINE)
                    .build();

            CCDCollectionElement<CCDRespondent> updatedElement =
                CCDCollectionElement.<CCDRespondent>builder()
                    .id(originalElement.getId())
                    .value(respondent)
                    .build();

            ccdCase.setRespondents(List.of(updatedElement));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }
}

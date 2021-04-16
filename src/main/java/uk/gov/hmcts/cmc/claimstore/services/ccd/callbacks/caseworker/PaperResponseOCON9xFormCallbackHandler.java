package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_OCON_9X_FORM;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod.OCON_FORM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;

@Service
public class PaperResponseOCON9xFormCallbackHandler  extends CallbackHandler {

    private static final String DYNAMIC_LIST_CODE = "code";
    private static final String DYNAMIC_LIST_LABEL = "label";
    private static final String DYNAMIC_LIST_ITEMS = "list_items";
    private static final String DYNAMIC_LIST_SELECTED_VALUE = "value";

    private static final String SCANNED_DOCUMENTS = "temporaryScannedDocuments";
    private static final String OCON9X = "ocon9xForm";
    public static final String OCON9X_SUBTYPE = "OCON9x";
    public static final String SCANNED_DOCUMENTS_MODIFIED_ERROR = "You may not add or remove new documents";

    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> EVENTS = List.of(PAPER_RESPONSE_OCON_9X_FORM);

    private final CaseDetailsConverter caseDetailsConverter;

    PaperResponseOCON9xFormCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            ABOUT_TO_START, this::filterScannedDocuments,
            MID, this::verifyNoDocumentsAddedOrRemoved,
            ABOUT_TO_SUBMIT, this::updateData
        );
    }

    private AboutToStartOrSubmitCallbackResponse filterScannedDocuments(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(request.getCaseDetails());

        List<CCDCollectionElement<CCDScannedDocument>> forms = filterForms(ccdCase);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(Map.of(SCANNED_DOCUMENTS, forms,
                         OCON9X, buildFilesList(forms, ccdCase.getOcon9xForm()))
            )
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse verifyNoDocumentsAddedOrRemoved(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(request.getCaseDetails());
        ccdCase.setTempOcon9xFormSelectedValue(ccdCase.getOcon9xForm());
        ccdCase.setOcon9xForm(null);
        List<CCDCollectionElement<CCDScannedDocument>> formsBefore = filterForms(ccdCase);

        List<String> errors = new ArrayList<>();
        if (!formsBefore.equals(ccdCase.getTemporaryScannedDocuments())) {
            errors.add(SCANNED_DOCUMENTS_MODIFIED_ERROR);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDetailsConverter.convertToMap(ccdCase))
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse updateData(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(request.getCaseDetails());
        CCDScannedDocument updatedDocument = ccdCase.getScannedDocuments()
            .stream()
            .filter(e -> e.getId().equals(ccdCase.getTempOcon9xFormSelectedValue()))
            .map(CCDCollectionElement::getValue)
            .map(d -> d.toBuilder()
                .fileName(String.format("%s-scanned-OCON9x-form.pdf", ccdCase.getPreviousServiceCaseReference()))
                .subtype(OCON9X_SUBTYPE)
                .build())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Selected OCON9x form id is not a scanned document"));

        List<CCDCollectionElement<CCDScannedDocument>> updatedScannedDocuments = ccdCase.getScannedDocuments()
            .stream()
            .map(e -> e.getId().equals(ccdCase.getTempOcon9xFormSelectedValue()) ? e.toBuilder().value(updatedDocument).build() : e)
            .collect(Collectors.toList());

        LocalDateTime mostRecentDeliveryDate = updatedScannedDocuments.stream()
            .map(CCDCollectionElement::getValue)
            .filter(d -> OCON9X_SUBTYPE.equals(d.getSubtype()))
            .map(CCDScannedDocument::getDeliveryDate)
            .max(LocalDateTime::compareTo)
            .orElseThrow(IllegalStateException::new);

        List<CCDCollectionElement<CCDRespondent>> updatedRespondents =
            ccdCase.getRespondents().stream()
                .map(r -> updateRespondent(r, mostRecentDeliveryDate))
                .collect(Collectors.toList());

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .respondents(updatedRespondents)
            .scannedDocuments(updatedScannedDocuments)
            .temporaryScannedDocuments(Collections.emptyList())
            .ocon9xForm(null)
            .tempOcon9xFormSelectedValue(null)
            .evidenceHandled(CCDYesNoOption.YES)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCCDCase))
            .build();
    }

    private CCDCollectionElement<CCDRespondent> updateRespondent(CCDCollectionElement<CCDRespondent> element,
                                                                 LocalDateTime deliveryDate) {
        CCDRespondent updatedRespondent = element.getValue()
            .toBuilder()
            .responseMethod(OCON_FORM)
            .responseSubmittedOn(deliveryDate)
            .build();

        return CCDCollectionElement.<CCDRespondent>builder()
            .id(element.getId())
            .value(updatedRespondent)
            .build();
    }

    private List<CCDCollectionElement<CCDScannedDocument>> filterForms(CCDCase ccdCase) {
        return ccdCase.getScannedDocuments().stream()
            .filter(e -> e.getValue().getType().equals(CCDScannedDocumentType.form))
            .filter(e -> isBlank(e.getValue().getSubtype()) || OCON9X_SUBTYPE.equals(e.getValue().getSubtype()))
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildFilesList(List<CCDCollectionElement<CCDScannedDocument>> forms,
                                               String ocon9xForm) {
        List<Map<String, String>> listItems = forms.stream()
            .sorted(Comparator.comparing(y -> y.getValue().getUrl().getDocumentFileName(), String::compareToIgnoreCase))
            .map(f -> Map.of(DYNAMIC_LIST_CODE, f.getId(),
                             DYNAMIC_LIST_LABEL, f.getValue().getUrl().getDocumentFileName())
            )
            .collect(Collectors.toList());

        if (isBlank(ocon9xForm)) {
            return Map.of(DYNAMIC_LIST_ITEMS, listItems);

        } else {
            Optional<Map<String, String>> selectedForm = listItems.stream()
                .filter(s -> s.get(DYNAMIC_LIST_CODE).equals(ocon9xForm))
                .findFirst();

            return Map.of(DYNAMIC_LIST_ITEMS, listItems, DYNAMIC_LIST_SELECTED_VALUE, selectedForm);
        }
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

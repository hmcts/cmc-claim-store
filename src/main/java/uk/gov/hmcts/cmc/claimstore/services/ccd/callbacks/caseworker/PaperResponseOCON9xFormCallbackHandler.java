package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_OCON_9X_FORM;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod.OCON_FORM;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType.MID;

@Service
public class PaperResponseOCON9xFormCallbackHandler  extends CallbackHandler {

    private static final String SCANNED_DOCUMENTS = "filteredScannedDocuments";
    public static final String OCON9X_SUBTYPE = "OCON9x";
    public static final String SCANNED_DOCUMENTS_MODIFIED_ERROR = "You man not add or remove documents";

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

        Map data = Map.of(SCANNED_DOCUMENTS, forms);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse verifyNoDocumentsAddedOrRemoved(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(request.getCaseDetails());
        CCDCase caseDetailsBefore = caseDetailsConverter.extractCCDCase(request.getCaseDetailsBefore());

        Set<String> scannedDocuments = ccdCase.getScannedDocuments()
            .stream()
            .map(CCDCollectionElement::getId)
            .collect(Collectors.toSet());

        Set<String> scannedDocumentsBeforeEvent = caseDetailsBefore.getScannedDocuments()
            .stream()
            .map(CCDCollectionElement::getId)
            .collect(Collectors.toSet());

        List errors = new ArrayList();
        if (!scannedDocuments.equals(scannedDocumentsBeforeEvent)) {
            errors.add(SCANNED_DOCUMENTS_MODIFIED_ERROR);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse updateData(CallbackParams callbackParams) {
        CallbackRequest request = callbackParams.getRequest();

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(request.getCaseDetails());

        List<CCDCollectionElement<CCDScannedDocument>> updatedScannedDocumentsWithSubtype =
            mergeUpdatedScannedDocuments(ccdCase.getScannedDocuments(), ccdCase.getFilteredScannedDocuments());

        List<CCDCollectionElement<CCDScannedDocument>> ocon9xDocuments = ccdCase.getFilteredScannedDocuments()
                .stream()
                .filter(e -> e.getValue().getSubtype().equals(OCON9X_SUBTYPE))
                .collect(Collectors.toList());

        if (ocon9xDocuments.isEmpty()) {
            CCDCase updatedCCDCase = ccdCase.toBuilder()
                .scannedDocuments(updatedScannedDocumentsWithSubtype)
                .filteredScannedDocuments(Collections.emptyList())
                .build();
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetailsConverter.convertToMap(updatedCCDCase))
                .build();
        }

        LocalDateTime mostRecentDeliveryDate = ocon9xDocuments.stream()
            .map(CCDCollectionElement::getValue)
            .map(CCDScannedDocument::getDeliveryDate)
            .max(LocalDateTime::compareTo)
            .orElseThrow(IllegalStateException::new);

        List<CCDCollectionElement<CCDScannedDocument>> ocon9xDocumentsWithUpdatedFilenames = ocon9xDocuments.stream()
            .map(e -> updateScannedDocumentFilename(e, ccdCase.getPreviousServiceCaseReference()))
            .collect(Collectors.toList());

        List<CCDCollectionElement<CCDScannedDocument>> updatedScannedDocuments =
            mergeUpdatedScannedDocuments(updatedScannedDocumentsWithSubtype, ocon9xDocumentsWithUpdatedFilenames);

        List<CCDCollectionElement<CCDRespondent>> updatedRespondents =
            ccdCase.getRespondents().stream()
                .map(r -> updateRespondent(r, mostRecentDeliveryDate))
                .collect(Collectors.toList());

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .respondents(updatedRespondents)
            .scannedDocuments(updatedScannedDocuments)
            .filteredScannedDocuments(Collections.emptyList())
            .evidenceHandled(CCDYesNoOption.YES)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCCDCase))
            .build();
    }

    private List<CCDCollectionElement<CCDScannedDocument>> mergeUpdatedScannedDocuments(
        List<CCDCollectionElement<CCDScannedDocument>> to,
        List<CCDCollectionElement<CCDScannedDocument>> from) {

        var documentsToMergeById = from.stream()
            .collect(Collectors.toMap(CCDCollectionElement::getId, element -> element));

        return to.stream()
            .map(element -> documentsToMergeById.getOrDefault(element.getId(), element))
            .collect(Collectors.toList());
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

    private CCDCollectionElement<CCDScannedDocument> updateScannedDocumentFilename(
        CCDCollectionElement<CCDScannedDocument> element,
        String caseReference
    ) {

        CCDScannedDocument document = element.getValue()
            .toBuilder()
            .fileName(String.format("%s-scanned-OCON9x-form.pdf", caseReference))
            .build();

        return element.toBuilder().value(document).build();
    }

    private List<CCDCollectionElement<CCDScannedDocument>> filterForms(CCDCase ccdCase) {
        return ccdCase.getScannedDocuments().stream()
            .filter(e -> e.getValue().getType().equals(CCDScannedDocumentType.form))
            .filter(e -> StringUtils.isBlank(e.getValue().getSubtype()))
            .collect(Collectors.toList());
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

package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_GENERAL_LETTER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildLetterFileBaseName;

@Service
@ConditionalOnProperty({"doc_assembly.url", "feature_toggles.ctsc_enabled"})
public class GeneralLetterCallbackHandler extends CallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(ISSUE_GENERAL_LETTER);
    private final GeneralLetterService generalLetterService;
    private final CaseDetailsConverter caseDetailsConverter;
    protected static final String DRAFT_LETTER_DOC = "draftLetterDoc";
    private final String generalLetterTemplateId;
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";

    @Autowired
    public GeneralLetterCallbackHandler(
        GeneralLetterService generalLetterService,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId,
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.generalLetterService = generalLetterService;
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.caseDetailsConverter = caseDetailsConverter;
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
        try {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(ImmutableMap.of(
                    DRAFT_LETTER_DOC,
                    CCDDocument.builder().documentUrl(generalLetterService.createAndPreview(
                        caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails()),
                        authorisation,
                        generalLetterTemplateId)).build()
                ))
                .build();
        } catch (DocumentGenerationFailedException e) {
            logger.info("General Letter creating and preview failed", e);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    public CallbackResponse printAndUpdateCaseDocuments(CallbackParams callbackParams) {
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        boolean errors = false;
        CCDCase updatedCcdCase = ccdCase;
        try {
            updatedCcdCase = generalLetterService.printAndUpdateCaseDocuments(
                ccdCase,
                caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails()),
                authorisation,
                getDocumentName(ccdCase)
            );
        } catch (Exception e) {
            logger.info("General Letter printing and case documents update failed", e);
            errors = true;
        }
        if (!errors) {
            logger.info("General Letter: updating case document with general letter");
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCcdCase))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    private String getDocumentName(CCDCase ccdCase) {
        Integer number = Math.toIntExact(ccdCase.getCaseDocuments()
                .stream()
                .map(CCDCollectionElement::getValue)
                .filter(c -> c.getDocumentType().equals(GENERAL_LETTER))
                .filter(c -> c.getDocumentName().contains(LocalDate.now().toString()))
                .count() + 1);
        return String.format("%s-%s.pdf", buildLetterFileBaseName(ccdCase.getPreviousServiceCaseReference(),
            LocalDate.now().toString()), number);
    }
}

package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForDefendant;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Service
public class PaperResponseAdmissionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(PAPER_RESPONSE_ADMISSION);
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final String FORM_NAME = "OCON9x";
    private static final String OCON_PAPER_FORM = "PaperResponseOCON9xForm";
    private static final String OCON9X_REVIEW_MSG =
        "Before you continue please esure you review the OCON9x review response";
    private final CaseDetailsConverter caseDetailsConverter;
    private final DefendantResponseNotificationService defendantResponseNotificationService;
    private final CaseMapper caseMapper;
    private final DocAssemblyService docAssemblyService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final String paperResponseAdmissionTemplateId;
    private final UserService userService;
    private final DocumentManagementService documentManagementService;
    private final Clock clock;
    private final GeneralLetterService generalLetterService;
    private final CaseEventService caseEventService;
    private final LaunchDarklyClient launchDarklyClient;

    private final Map<CallbackType, Callback> callbacks = Map.of(
        CallbackType.ABOUT_TO_START, this::aboutToStart,
        CallbackType.ABOUT_TO_SUBMIT, this::aboutToSubmit
    );

    public PaperResponseAdmissionCallbackHandler(CaseDetailsConverter caseDetailsConverter,
             DefendantResponseNotificationService defendantResponseNotificationService,
             CaseMapper caseMapper,
             DocAssemblyService docAssemblyService,
             DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
             @Value("${doc_assembly.paperResponseAdmissionTemplateId}") String paperResponseAdmissionTemplateId,
             UserService userService,
             DocumentManagementService documentManagementService,
             Clock clock,
             GeneralLetterService generalLetterService,
             CaseEventService caseEventService,
             LaunchDarklyClient launchDarklyClient) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.defendantResponseNotificationService = defendantResponseNotificationService;
        this.caseMapper = caseMapper;
        this.docAssemblyService = docAssemblyService;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.paperResponseAdmissionTemplateId = paperResponseAdmissionTemplateId;
        this.userService = userService;
        this.documentManagementService = documentManagementService;
        this.clock = clock;
        this.generalLetterService = generalLetterService;
        this.caseEventService = caseEventService;
        this.launchDarklyClient = launchDarklyClient;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (launchDarklyClient.isFeatureEnabled("ocon-enhancements", LaunchDarklyClient.CLAIM_STORE_USER)) {
            List<CaseEvent> caseEvents = caseEventService.findEventsForCase(
                String.valueOf(ccdCase.getId()), userService.getUser(authorisation));
            boolean isPresent = caseEvents.stream()
                .anyMatch(event -> event.getValue().equals(OCON_PAPER_FORM));
            if (!isPresent) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(OCON9X_REVIEW_MSG)).build();
            }
        }
        Map<String, Object> data = new HashMap<>(caseDetailsConverter.convertToMap(ccdCase));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        List<CCDCollectionElement<CCDScannedDocument>> updatedCCDScannedDocs = ccdCase.getScannedDocuments()
            .stream()
            .map(e -> e.getValue().getSubtype().equals(FORM_NAME) ? renameFile(e, ccdCase) : e)
            .collect(Collectors.toList());
        List<CCDCollectionElement<CCDRespondent>> updatedRespondent = ccdCase.getRespondents()
            .stream()
            .map(e -> e.toBuilder()
                .value(e.getValue()
                    .toBuilder()
                    .responseType(ccdCase.getPaperAdmissionType())
                    .partyDetail(getPartyDetail(e))
                    .responseSubmittedOn(updatedCCDScannedDocs.stream()
                        .map(CCDCollectionElement::getValue)
                        .filter(s -> s.getSubtype().equals(FORM_NAME))
                        .map(CCDScannedDocument::getDeliveryDate)
                        .max(LocalDateTime::compareTo)
                        .orElseThrow(IllegalStateException::new))
                    .build())
                .build())
            .collect(Collectors.toList());

        CCDCase updatedCCDCase = ccdCase.toBuilder()
            .scannedDocuments(updatedCCDScannedDocs)
            .respondents(updatedRespondent)
            .paperAdmissionType(null)
            .build();

        Claim claim = caseMapper.from(updatedCCDCase);
        if (isDefendentLinked(updatedCCDCase)) {
            sendDefendantEmail(updatedCCDCase, claim);
        } else {
            updatedCCDCase = sendDefendantLetter(callbackParams, updatedCCDCase, claim);
        }

        sendClaimantEmail(claim);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(updatedCCDCase))
            .build();
    }

    private CCDParty getPartyDetail(CCDCollectionElement<CCDRespondent> e) {
        return e.getValue().getPartyDetail() != null ? e.getValue().getPartyDetail().toBuilder()
            .type(e.getValue().getClaimantProvidedDetail().getType())
            .build() : e.getValue().getClaimantProvidedDetail().toBuilder().build();
    }

    private boolean isDefendentLinked(CCDCase updatedCCDCase) {
        return !StringUtils.isBlank(updatedCCDCase.getRespondents().get(0).getValue().getDefendantId());
    }

    private void sendClaimantEmail(Claim claim) {
        defendantResponseNotificationService.notifyClaimant(
            claim,
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private CCDCase sendDefendantLetter(CallbackParams callbackParams, CCDCase updatedCCDCase, Claim claim) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        var docAssemblyResponse = docAssemblyService.renderTemplate(updatedCCDCase, authorisation,
            paperResponseAdmissionTemplateId,
            docAssemblyTemplateBodyMapper.paperResponseAdmissionLetter(updatedCCDCase,
                userService.getUserDetails(authorisation).getFullName()));

        var documentMetadata = documentManagementService.getDocumentMetaData(
            authorisation,
            URI.create(docAssemblyResponse.getRenditionOutputLocation()).getPath()
        );

        String documentName = String.format("%s-defendant-case-handoff.pdf",
            updatedCCDCase.getPreviousServiceCaseReference());

        CCDDocument ccdDocument = CCDDocument.builder()
            .documentFileName(documentName)
            .documentBinaryUrl(documentMetadata.links.binary.href)
            .documentUrl(docAssemblyResponse.getRenditionOutputLocation())
            .build();

        printLetter(claim, authorisation, ccdDocument);

        CCDCollectionElement<CCDClaimDocument> claimDocument = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(ccdDocument)
                .documentName(documentName)
                .createdDatetime(LocalDateTime.now(clock.withZone(UTC_ZONE)))
                .size(documentMetadata.size)
                .documentType(GENERAL_LETTER)
                .build())
            .build();

        return updatedCCDCase.toBuilder()
            .caseDocuments(ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
                .addAll(updatedCCDCase.getCaseDocuments())
                .add(claimDocument)
                .build())
            .build();
    }

    private void printLetter(Claim claim, String authorisation, CCDDocument ccdDocument) {
        generalLetterService.printLetter(authorisation, ccdDocument, claim);
    }

    private void sendDefendantEmail(CCDCase updatedCCDCase, Claim claim) {
        defendantResponseNotificationService.notifyDefendant(
            claim,
            updatedCCDCase.getRespondents().get(0).getValue().getPartyDetail().getEmailAddress(),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private CCDCollectionElement<CCDScannedDocument> renameFile(CCDCollectionElement<CCDScannedDocument> element,
                                                                CCDCase ccdCase) {
        boolean isFullAdmission = ccdCase.getPaperAdmissionType() == FULL_ADMISSION;

        String fileName = String.format("%s-scanned-OCON9x-%s-admission.pdf",
            ccdCase.getPreviousServiceCaseReference(), isFullAdmission ? "full" : "part");

        return element.toBuilder()
            .value(element.getValue()
                .toBuilder()
                .fileName(fileName)
                .build())
            .build();
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
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

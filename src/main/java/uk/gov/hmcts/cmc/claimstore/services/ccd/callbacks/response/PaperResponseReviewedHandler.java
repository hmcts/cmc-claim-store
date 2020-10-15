package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaperResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.Lazy;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Claim.ClaimBuilder;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.utils.Lazy.lazily;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.COVERSHEET;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.LETTER;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.OTHER;

class PaperResponseReviewedHandler {
    private static final List<ClaimDocumentType> PAPER_RESPONSE_STAFF_UPLOADED_TYPES = List.of(
        ClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL,
        ClaimDocumentType.PAPER_RESPONSE_PART_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_STATES_PAID);

    private static final List<String> PAPER_RESPONSE_SCANNED_TYPES = List.of("N9a", "N9b", "N11", "N225", "N180");

    private static final List<String> responseForms = List.of("N9", "N9a", "N9b", "N11");

    private static final List<String> nonResponseForms = List.of("N180", "N225", "EX160", "N244", "N245", "Non_prescribed_documents");

    private static final List<ScannedDocumentType> otherDocumentTypes = List.of(LETTER, OTHER);

    private static final Predicate<ClaimDocument> isPaperResponseClaimDoc = doc ->
        PAPER_RESPONSE_STAFF_UPLOADED_TYPES.stream().anyMatch(isEqual(doc.getDocumentType()));

    private static final Predicate<ScannedDocument> isPaperResponseScannedDoc = doc ->
        PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype()));

    private static final Predicate<ClaimDocument> isClaimDocumentMoreTimeRequested = doc ->
        ClaimDocumentType.PAPER_RESPONSE_MORE_TIME.equals(doc.getDocumentType());

    private static final Predicate<ScannedDocument> isScannedDocumentMoreTimeRequested = doc ->
        "N9".equalsIgnoreCase(doc.getSubtype());

    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;

    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final NotificationService notificationService;
    private final EmailTemplates mailTemplates;
    private final String frontendBaseUrl;

    private final Lazy<Claim> claimBeforeEvent;
    private final Lazy<Claim> claimAfterEvent;

    private final ClaimBuilder claim;
    private final List<String> errors = new ArrayList<>();

    PaperResponseReviewedHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        CallbackParams callbackParams
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.notificationService = notificationService;
        this.mailTemplates = notificationsProperties.getTemplates().getEmail();
        this.frontendBaseUrl = notificationsProperties.getFrontendBaseUrl();

        CallbackRequest callbackRequest = callbackParams.getRequest();
        claimBeforeEvent = lazily(() -> toClaimBeforeEvent(callbackRequest));
        claimAfterEvent = lazily(() -> toClaimAfterEvent(callbackRequest));
        claim = claimAfterEvent.get().toBuilder();
    }

    AboutToStartOrSubmitCallbackResponse handle() {
        if (hasRequestedMoreTimeRepeatedly()) {
            errors.add("Requesting More Time to respond can be done only once.");
        }

        if (hasMoreTimeRequestedForEvent()) {
            updateMoreTimeRequestedResponse();
        }

        getResponseTimeFromPaperResponse(claimAfterEvent.get())
            .ifPresent(claim::respondedAt);

        AboutToStartOrSubmitCallbackResponseBuilder response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim.build())));

        if (! errors.isEmpty()) {
            return response.errors(errors).build();
        }

        return email(response).build();
    }

    private AboutToStartOrSubmitCallbackResponseBuilder email(AboutToStartOrSubmitCallbackResponseBuilder response) {
        final Optional<ScannedDocument> scannedDocument = getScannedDocument();
        if (scannedDocument.isPresent() && ! COVERSHEET.equals(scannedDocument.get().getDocumentType())) {
            String subType = scannedDocument.get().getSubtype();
            if (subType != null && responseForms.contains(subType)) {
                response = response.state(ClaimState.BUSINESS_QUEUE.getValue());
                notifyClaimant(claim.build(), mailTemplates.getPaperResponseReceivedAndCaseTransferredToCCBC());
            } else if (subType != null && nonResponseForms.contains(subType)) {
                notifyClaimant(claim.build(), mailTemplates.getPaperResponseReceivedAndCaseWillBeTransferredToCCBC());
            } else if (otherDocumentTypes.contains(scannedDocument.get().getDocumentType())) {
                notifyClaimant(claim.build(), mailTemplates.getClaimantPaperResponseReceivedGeneralResponse());
            }
        } else {
            notifyClaimant(claim.build(), mailTemplates.getClaimantPaperResponseReceived());
        }
        return response;
    }

    private Optional<ScannedDocument> getScannedDocument() {
        return getBulkScannedDocuments(claimAfterEvent.get())
            .filter(doc -> getBulkScannedDocuments(claimBeforeEvent.get()).noneMatch(isEqual(doc))).findFirst();
    }


    private Claim toClaimAfterEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }

    private Claim toClaimBeforeEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
    }

    private void updateMoreTimeRequestedResponse() {
        Claim claimByEvent = claimAfterEvent.get();
        LocalDate deadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claimByEvent.getIssuedOn());

        errors.addAll(moreTimeRequestRule.validateMoreTimeCanBeRequested(claimByEvent, deadline));
        claim
            .responseDeadline(deadline)
            .moreTimeRequested(true);
    }

    private boolean hasRequestedMoreTimeRepeatedly() {
        return Stream.concat(getMoreTimeRequestedStaffUploadedDocs(), getMoreTimeRequestedBulkScanDocs())
            .count() > 1;
    }

    private boolean hasMoreTimeRequestedForEvent() {
        boolean uploaded = getStaffUploadedDocuments(claimAfterEvent.get())
            .filter(doc -> getStaffUploadedDocuments(claimBeforeEvent.get()).noneMatch(isEqual(doc)))
            .anyMatch(isClaimDocumentMoreTimeRequested);
        boolean scanned = getBulkScannedDocuments(claimAfterEvent.get())
            .filter(doc -> getBulkScannedDocuments(claimBeforeEvent.get()).noneMatch(isEqual(doc)))
            .anyMatch(isScannedDocumentMoreTimeRequested);
        return uploaded || scanned;
    }

    private void notifyClaimant(Claim claim, String templateId) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            templateId,
            aggregateParams(claim),
            PaperResponse.notifyClaimantPaperResponseSubmitted(claim.getReferenceNumber(), "claimant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return Map.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            FRONTEND_BASE_URL, frontendBaseUrl,
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    private static Optional<LocalDateTime> getResponseTimeFromPaperResponse(Claim claim) {
        return Optional.ofNullable(getStaffUploadedPaperResponseDoc(claim)
            .map(ClaimDocument::getReceivedDateTime)
            .orElseGet(() -> getScannedPaperResponseDoc(claim)
                .map(ScannedDocument::getDeliveryDate)
                .orElse(null)
            ));
    }

    private Stream<ClaimDocument> getMoreTimeRequestedStaffUploadedDocs() {
        return getStaffUploadedDocuments(claimAfterEvent.get())
            .filter(isClaimDocumentMoreTimeRequested);
    }

    private Stream<ScannedDocument> getMoreTimeRequestedBulkScanDocs() {
        return getBulkScannedDocuments(claimAfterEvent.get())
            .filter(isScannedDocumentMoreTimeRequested);
    }

    private static Optional<ClaimDocument> getStaffUploadedPaperResponseDoc(Claim claim) {
        return getStaffUploadedDocuments(claim)
            .filter(isPaperResponseClaimDoc)
            .findFirst();
    }

    private static Optional<ScannedDocument> getScannedPaperResponseDoc(Claim claim) {
        return getBulkScannedDocuments(claim)
            .filter(isPaperResponseScannedDoc)
            .findFirst();
    }

    private static Stream<ClaimDocument> getStaffUploadedDocuments(Claim claim) {
        return claim.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getStaffUploadedDocuments)
            .stream()
            .flatMap(List::stream);
    }

    private static Stream<ScannedDocument> getBulkScannedDocuments(Claim claim) {
        return claim.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getScannedDocuments)
            .stream()
            .flatMap(List::stream);
    }
}

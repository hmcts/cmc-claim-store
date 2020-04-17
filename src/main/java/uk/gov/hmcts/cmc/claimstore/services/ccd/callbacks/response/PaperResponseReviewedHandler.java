package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaperResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.Lazy;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.utils.Lazy.lazily;

public class PaperResponseReviewedHandler {
    private static final List<ClaimDocumentType> paperResponseStaffUploadedTypes = List.of(
        ClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL,
        ClaimDocumentType.PAPER_RESPONSE_PART_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_STATES_PAID);

    private static final List<String> paperResponseScannedTypes = List.of("N9a", "N9b", "N11", "N225", "N180");

    private static final Predicate<ClaimDocument> filterClaimDocumentPaperResponseDoc = doc ->
        paperResponseStaffUploadedTypes.stream().anyMatch(isEqual(doc.getDocumentType()));

    private static final Predicate<ScannedDocument> filterScannedDocumentPaperResponseDoc = doc ->
        paperResponseScannedTypes.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype()));

    private static final Predicate<ClaimDocument> isClaimDocumentMoreTimeRequested = doc ->
        ClaimDocumentType.PAPER_RESPONSE_MORE_TIME.equals(doc.getDocumentType());

    private static final Predicate<ScannedDocument> isScannedDocumentMoreTimeRequested = doc ->
        "N9".equalsIgnoreCase(doc.getSubtype());

    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;

    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    private final Lazy<Claim> claimBeforeEvent;
    private final Lazy<Claim> claimAfterEvent;

    public PaperResponseReviewedHandler(
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
        this.notificationsProperties = notificationsProperties;

        CallbackRequest callbackRequest = callbackParams.getRequest();
        claimBeforeEvent = lazily(() -> toClaimBeforeEvent(callbackRequest));
        claimAfterEvent = lazily(() -> toClaimAfterEvent(callbackRequest));
    }

    public AboutToStartOrSubmitCallbackResponse handle() {
        Claim claim = claimAfterEvent.get();

        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();

        if (verifyNoDuplicateMoreTimeRequested()) {
            return responseBuilder
                .errors(List.of("Requesting More Time to respond can be done only once."))
                .build();
        }

        return updateMoreTimeRequestedResponse().orElseGet(() -> {
            try {
                getResponseTimeFromPaperResponse(claim).ifPresent(paperResponseTime -> {
                    Claim updatedClaim = claim.toBuilder().respondedAt(paperResponseTime).build();
                    CCDCase ccdCase = caseMapper.to(updatedClaim);
                    var data = caseDetailsConverter.convertToMap(ccdCase);
                    responseBuilder.data(data);
                    notifyClaimant(updatedClaim);
                });
            } catch (Exception ex) {
                responseBuilder.errors(List.of("Unable to determine the response time.")).build();
            }
            return responseBuilder.build();
        });
    }

    private Claim toClaimAfterEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }

    private Claim toClaimBeforeEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
    }

    private boolean verifyNoDuplicateMoreTimeRequested() {
        return getMoreTimeRequestedStaffUploadedDocs(claimAfterEvent.get()).size()
            + getMoreTimeRequestedBulkScanDocs(claimAfterEvent.get()).size() > 1;
    }

    private Optional<AboutToStartOrSubmitCallbackResponse> updateMoreTimeRequestedResponse() {
        Claim claimByEvent = claimAfterEvent.get();

        if (getStaffUploadedMoreTimeRequestedForEvent().isEmpty()
            && getBulkScannedDocMoreTimeRequestedForEvent().isEmpty()) {
            return Optional.empty();
        }

        LocalDate newDeadline =
            responseDeadlineCalculator.calculatePostponedResponseDeadline(claimByEvent.getIssuedOn());

        List<String> validationResult = moreTimeRequestRule.validateMoreTimeCanBeRequested(claimByEvent, newDeadline);

        if (!validationResult.isEmpty()) {
            return Optional.of(AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(validationResult)
                .build());
        }

        Claim updatedClaimByEvent = claimByEvent.toBuilder()
            .responseDeadline(newDeadline)
            .moreTimeRequested(true)
            .build();
        Map<String, Object> data = caseDetailsConverter.convertToMap(caseMapper.to(updatedClaimByEvent));

        return Optional.of(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(data).build()
        );
    }

    private List<ClaimDocument> getStaffUploadedMoreTimeRequestedForEvent() {
        return getStaffUploadedDocuments(claimAfterEvent.get())
            .filter(doc -> getStaffUploadedDocuments(claimBeforeEvent.get()).noneMatch(isEqual(doc)))
            .filter(isClaimDocumentMoreTimeRequested)
            .collect(toList());
    }

    private List<ScannedDocument> getBulkScannedDocMoreTimeRequestedForEvent() {
        return getBulkScannedDocuments(claimAfterEvent.get())
            .filter(doc -> getBulkScannedDocuments(claimBeforeEvent.get()).noneMatch(isEqual(doc)))
            .filter(isScannedDocumentMoreTimeRequested)
            .collect(toList());
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantPaperResponseReceived(),
            aggregateParams(claim),
            PaperResponse.notifyClaimantPaperResponseSubmitted(claim.getReferenceNumber(), "claimant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return Map.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl(),
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    private static Optional<LocalDateTime> getResponseTimeFromPaperResponse(Claim claim) {
        return Optional.ofNullable(getStaffUploadedPaperResponseDoc(claim)
            .map(ClaimDocument::getReceivedDateTime)
            .orElseGet(() -> getScannedTimeForScannedPaperResponseDoc(claim)
                .map(ScannedDocument::getDeliveryDate)
                .orElse(null)
            ));
    }

    private static List<ClaimDocument> getMoreTimeRequestedStaffUploadedDocs(Claim claim) {
        return getStaffUploadedDocuments(claim)
            .filter(isClaimDocumentMoreTimeRequested)
            .collect(toList());
    }

    private static List<ScannedDocument> getMoreTimeRequestedBulkScanDocs(Claim claim) {
        return getBulkScannedDocuments(claim)
            .filter(isScannedDocumentMoreTimeRequested)
            .collect(toList());
    }

    private static Optional<ClaimDocument> getStaffUploadedPaperResponseDoc(Claim claim) {
        return getStaffUploadedDocuments(claim)
            .filter(filterClaimDocumentPaperResponseDoc)
            .findFirst();
    }

    private static Optional<ScannedDocument> getScannedTimeForScannedPaperResponseDoc(Claim claim) {
        return getBulkScannedDocuments(claim)
            .filter(filterScannedDocumentPaperResponseDoc)
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

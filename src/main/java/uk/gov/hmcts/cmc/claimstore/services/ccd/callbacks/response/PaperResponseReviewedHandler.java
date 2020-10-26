package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaperResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Claim.ClaimBuilder;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.List.of;
import static java.util.function.Predicate.isEqual;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.PAPER_RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.LETTER;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.OTHER;

@Service
class PaperResponseReviewedHandler {

    private static final List<String> responseForms = of("N9a", "N9b", "N11");
    private static final List<String> forms = of("N180", "N225", "EX160", "N244", "N245", "Non_prescribed_documents");
    private static final List<String> SCANNED_DOCUMENT_TYPES = newArrayList(concat(responseForms, forms));

    private static final List<ClaimDocumentType> STAFF_UPLOADED_DOCS = of(
        ClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL,
        ClaimDocumentType.PAPER_RESPONSE_PART_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_STATES_PAID);

    private static final List<ScannedDocumentType> otherDocumentTypes = of(LETTER, OTHER);

    private final CaseMapper caseMapper;
    private final NotificationsProperties notificationsProperties;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    PaperResponseReviewedHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        LaunchDarklyClient launchDarklyClient
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.launchDarklyClient = launchDarklyClient;
    }

    AboutToStartOrSubmitCallbackResponse handle(final CallbackParams callbackParams) {

        final CallbackRequest callbackRequest = callbackParams.getRequest();
        final Claim beforeClaim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
        final Claim afterClaim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        final ClaimBuilder claimBuilder = afterClaim.toBuilder();

        final List<String> errors = new ArrayList<>();

        if (hasRequestedMoreTimeRepeatedly(afterClaim)) {
            errors.add("Requesting More Time to respond can be done only once.");
        }

        if (hasMoreTimeRequestedForEvent(beforeClaim, afterClaim)) {
            updateMoreTimeRequestedResponse(claimBuilder, afterClaim, errors);
        }

        getResponseTimeFromPaperResponse(afterClaim)
            .ifPresent(claimBuilder::respondedAt);

        AboutToStartOrSubmitCallbackResponseBuilder response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claimBuilder.build())));

        if (!errors.isEmpty()) {
            return response.errors(errors).build();
        }

        if (launchDarklyClient.isFeatureEnabled("paper-response-review-new-handling")) {
            email(response, beforeClaim, afterClaim);
        } else {
            EmailTemplates mailTemplates = notificationsProperties.getTemplates().getEmail();
            notifyClaimant(afterClaim, mailTemplates.getClaimantPaperResponseReceived());
        }

        return response.build();
    }

    private void email(AboutToStartOrSubmitCallbackResponseBuilder response, Claim beforeClaim, Claim afterClaim) {
        EmailTemplates mailTemplates = notificationsProperties.getTemplates().getEmail();
        Set<String> mailTemplateIds = getUploadedScannedDocuments(beforeClaim, afterClaim).stream()
            .map(scannedDocument -> {
                String subType = scannedDocument.getSubtype();
                if (subType != null && responseForms.contains(subType)) {
                    response.state(ClaimState.BUSINESS_QUEUE.getValue());
                    return mailTemplates.getPaperResponseReceivedAndCaseTransferredToCCBC();
                } else if (subType != null && forms.contains(subType)) {
                    return mailTemplates.getPaperResponseReceivedAndCaseWillBeTransferredToCCBC();
                } else if (otherDocumentTypes.contains(scannedDocument.getDocumentType())) {
                    return mailTemplates.getClaimantPaperResponseReceivedGeneralResponse();
                } else {
                    return mailTemplates.getClaimantPaperResponseReceived();
                }
            }).collect(Collectors.toSet());

        if (mailTemplateIds.size() > 0) {
            mailTemplateIds.forEach(mailTemplateId -> notifyClaimant(afterClaim, mailTemplateId));
        } else if (getDocumentsUploadedByStaff(beforeClaim, afterClaim).size() > 0) {
            notifyClaimant(afterClaim, mailTemplates.getClaimantPaperResponseReceived());
        }
    }

    private List<ScannedDocument> getUploadedScannedDocuments(final Claim beforeClaim, final Claim afterClaim) {
        return getBulkScannedDocuments(afterClaim)
            .filter(doc -> getBulkScannedDocuments(beforeClaim).noneMatch(isEqual(doc))).collect(Collectors.toList());
    }

    private List<ClaimDocument> getDocumentsUploadedByStaff(final Claim beforeClaim, final Claim afterClaim) {
        return getStaffUploadedDocuments(afterClaim)
            .filter(doc -> getStaffUploadedDocuments(beforeClaim).noneMatch(isEqual(doc))).collect(Collectors.toList());
    }

    private void updateMoreTimeRequestedResponse(final ClaimBuilder builder, Claim claim, final List<String> errors) {
        LocalDate deadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());
        errors.addAll(moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, deadline));
        builder.responseDeadline(deadline).moreTimeRequested(true);
    }

    private boolean hasRequestedMoreTimeRepeatedly(final Claim claim) {
        return Stream.concat(getMoreTimeRequestedStaffUploadedDocs(claim), getMoreTimeRequestedBulkScanDocs(claim))
            .count() > 1;
    }

    private boolean hasMoreTimeRequestedForEvent(final Claim beforeClaim, final Claim afterClaim) {
        boolean uploaded = getStaffUploadedDocuments(afterClaim)
            .filter(doc -> getStaffUploadedDocuments(beforeClaim).noneMatch(isEqual(doc)))
            .anyMatch(doc -> PAPER_RESPONSE_MORE_TIME.equals(doc.getDocumentType()));
        boolean scanned = getBulkScannedDocuments(afterClaim)
            .filter(doc -> getBulkScannedDocuments(beforeClaim).noneMatch(isEqual(doc)))
            .anyMatch(doc -> "N9".equals(doc.getSubtype()));
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
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl(),
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

    private Stream<ClaimDocument> getMoreTimeRequestedStaffUploadedDocs(final Claim claim) {
        return getStaffUploadedDocuments(claim)
            .filter(doc -> PAPER_RESPONSE_MORE_TIME.equals(doc.getDocumentType()));
    }

    private Stream<ScannedDocument> getMoreTimeRequestedBulkScanDocs(final Claim claim) {
        return getBulkScannedDocuments(claim)
            .filter(doc -> "N9".equals(doc.getSubtype()));
    }

    private static Optional<ClaimDocument> getStaffUploadedPaperResponseDoc(Claim claim) {
        return getStaffUploadedDocuments(claim)
            .filter(doc -> STAFF_UPLOADED_DOCS.contains(doc.getDocumentType()))
            .findFirst();
    }

    private static Optional<ScannedDocument> getScannedPaperResponseDoc(Claim claim) {
        return getBulkScannedDocuments(claim)
            .filter(doc -> SCANNED_DOCUMENT_TYPES.contains(doc.getSubtype()))
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

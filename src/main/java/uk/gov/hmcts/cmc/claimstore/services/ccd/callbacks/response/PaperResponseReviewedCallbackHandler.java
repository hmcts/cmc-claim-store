package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class PaperResponseReviewedCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;

    private static List<ClaimDocumentType> paperResponseStaffUploadedType = Arrays.asList(
        ClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL,
        ClaimDocumentType.PAPER_RESPONSE_PART_ADMIT,
        ClaimDocumentType.PAPER_RESPONSE_STATES_PAID);

    private static List<String> paperResponseScannedType = Arrays.asList("N9a",
        "N9b",
        "N11",
        "N225",
        "N180");

    private static Function<Claim, List<ClaimDocument>> getStaffUploadedDocs = claim ->
        claim.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getStaffUploadedDocuments)
            .orElse(Collections.emptyList());

    private static Function<Claim, List<ScannedDocument>> getBulkScannedDocs = claim ->
        claim.getClaimDocumentCollection()
            .map(ClaimDocumentCollection::getScannedDocuments)
            .orElse(Collections.emptyList());

    private static Predicate<ClaimDocument> filterClaimDocumentPaperResponseDoc = doc ->
        paperResponseStaffUploadedType.stream()
            .anyMatch(type -> type.equals(doc.getDocumentType()));

    private static Predicate<ScannedDocument> filterScannedDocumentPaperResponseDoc = doc ->
        paperResponseScannedType.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype()));

    private static Predicate<ClaimDocument> isClaimDocumentMoreTimeRequested = doc ->
        doc.getDocumentType().equals(ClaimDocumentType.PAPER_RESPONSE_MORE_TIME);

    private static Predicate<ScannedDocument> isScannedDocumentMoreTimeRequested = doc ->
        doc.getSubtype().equalsIgnoreCase("N9");

    private static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request "
        + "because the defendant already responded to the claim";

    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;

    @Autowired
    public PaperResponseReviewedCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::verifyResponsePossible,
            CallbackType.ABOUT_TO_SUBMIT, this::updateResponseDeadline
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.REVIEWED_PAPER_RESPONSE);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse updateResponseDeadline(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = toClaimAfterEvent(callbackRequest);

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (verifyNoDuplicateMoreTimeRequested(callbackRequest)) {
            return responseBuilder
                .errors(Collections.singletonList("Requesting More Time to respond can be done only once."))
                .build();
        }

        Optional<AboutToStartOrSubmitCallbackResponse> isPaperResponseMoreTimeRequested =
            updateMoreTimeRequestedResponse(callbackRequest);

        if (isPaperResponseMoreTimeRequested.isPresent()) {
            return isPaperResponseMoreTimeRequested.orElseThrow(IllegalArgumentException::new);
        }

        try {
            Optional<LocalDateTime> paperResponseTime = getResponseTimeFromPaperResponse(claim);

            if (paperResponseTime.isPresent()) {
                responseBuilder.data(caseDetailsConverter.convertToMap(
                    caseMapper.to(
                        claim.toBuilder()
                            .respondedAt(paperResponseTime.orElseThrow(IllegalArgumentException::new)).build()
                    )));
            }

            return responseBuilder.build();

        } catch (Exception gene) {
            return responseBuilder
                .errors(Collections.singletonList("Unable to determine the response time."))
                .build();
        }
    }

    private AboutToStartOrSubmitCallbackResponse verifyResponsePossible(
        CallbackParams callbackParams) {
        Claim claim = toClaimAfterEvent(callbackParams.getRequest());

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (claim.getResponse().isPresent() || claim.getRespondedAt() != null) {
            responseBuilder.errors(Collections.singletonList(ALREADY_RESPONDED_ERROR));
        }

        return responseBuilder.build();
    }

    private Claim toClaimAfterEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }

    private Claim toClaimBeforeEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetailsBefore());
    }

    private List<ClaimDocument> getMoreTimeRequestedStaffUploadedDocs(Claim claim) {
        return getStaffUploadedDocs.apply(claim)
            .stream()
            .filter(isClaimDocumentMoreTimeRequested)
            .collect(Collectors.toList());
    }

    private List<ScannedDocument> getMoreTimeRequestedBulkScanDocs(Claim claim) {
        return getBulkScannedDocs.apply(claim)
            .stream()
            .filter(isScannedDocumentMoreTimeRequested)
            .collect(Collectors.toList());
    }

    private List<ClaimDocument> getStaffUploadedMoreTimeRequestedForEvent(CallbackRequest callbackRequest) {
        return getStaffUploadedDocs.apply(toClaimAfterEvent(callbackRequest))
            .stream().filter(doc -> !getStaffUploadedDocs.apply(toClaimBeforeEvent(callbackRequest)).contains(doc))
            .filter(isClaimDocumentMoreTimeRequested)
            .collect(Collectors.toList());
    }

    private List<ScannedDocument> getBulkScannedDocMoreTimeRequestedForEvent(CallbackRequest callbackRequest) {
        return getBulkScannedDocs.apply(toClaimAfterEvent(callbackRequest))
            .stream().filter(doc -> !getBulkScannedDocs.apply(toClaimBeforeEvent(callbackRequest)).contains(doc))
            .filter(isScannedDocumentMoreTimeRequested)
            .collect(Collectors.toList());
    }

    private Optional<ClaimDocument> getReceivedTimeForStaffUploadedPaperResponseDoc(Claim claim) {
        return getStaffUploadedDocs.apply(claim)
            .stream()
            .filter(filterClaimDocumentPaperResponseDoc)
            .findFirst();
    }

    private Optional<ScannedDocument> getScannedTimeForScannedPaperResponseDoc(Claim claim) {
        return getBulkScannedDocs.apply(claim)
            .stream()
            .filter(filterScannedDocumentPaperResponseDoc)
            .findFirst();
    }

    private Optional<LocalDateTime> getResponseTimeFromPaperResponse(Claim claim) {
        return Optional.ofNullable(getReceivedTimeForStaffUploadedPaperResponseDoc(claim)
            .map(ClaimDocument::getReceivedDateTime)
            .orElseGet(() -> getScannedTimeForScannedPaperResponseDoc(claim)
                .map(ScannedDocument::getDeliveryDate)
                .orElse(null)
            ));
    }

    private boolean verifyNoDuplicateMoreTimeRequested(CallbackRequest request) {

        return getMoreTimeRequestedStaffUploadedDocs(toClaimAfterEvent(request)).size()
            + getMoreTimeRequestedBulkScanDocs(toClaimAfterEvent(request)).size() > 1;
    }

    private Optional<AboutToStartOrSubmitCallbackResponse> updateMoreTimeRequestedResponse(CallbackRequest request) {
        Claim claimByEvent = toClaimAfterEvent(request);

        if (getStaffUploadedMoreTimeRequestedForEvent(request).isEmpty()
            && getBulkScannedDocMoreTimeRequestedForEvent(request).isEmpty()) {
            return Optional.empty();
        }

        LocalDate newDeadline =
            responseDeadlineCalculator.calculatePostponedResponseDeadline(claimByEvent.getIssuedOn());

        List<String> validationResult = moreTimeRequestRule.validateMoreTimeCanBeRequested(claimByEvent, newDeadline);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse.builder();
        claimByEvent = claimByEvent.toBuilder()
            .responseDeadline(newDeadline)
            .moreTimeRequested(true)
            .build();

        if (!validationResult.isEmpty()) {
            return Optional.of(AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(validationResult)
                .build());
        }

        Map<String, Object> data = caseDetailsConverter.convertToMap(caseMapper.to(claimByEvent));

        return Optional.of(builder
            .data(data)
            .build());
    }
}

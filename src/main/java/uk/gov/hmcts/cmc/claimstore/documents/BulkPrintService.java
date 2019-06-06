package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;

@Service
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService implements PrintService {

    /* This is configured on Xerox end so they know its us printing and controls things
     like paper quality and resolution */
    protected static final String XEROX_TYPE_PARAMETER = "CMC001";

    protected static final String ADDITIONAL_DATA_LETTER_TYPE_KEY = "letterType";
    protected static final String ADDITIONAL_DATA_LETTER_TYPE_VALUE = "first-contact-pack";
    protected static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";
    protected static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final BulkPrintStaffNotificationService bulkPrintStaffNotificationService;
    private final AppInsights appInsights;
    private final boolean asyncEventProcessingEnabled;

    @Autowired
    public BulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator,
        BulkPrintStaffNotificationService bulkPrintStaffNotificationService,
        AppInsights appInsights,
        @Value("${feature_toggles.async_event_operations_enabled:false}") boolean asyncEventProcessingEnabled
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
        this.bulkPrintStaffNotificationService = bulkPrintStaffNotificationService;
        this.appInsights = appInsights;
        this.asyncEventProcessingEnabled = asyncEventProcessingEnabled;
    }

    @LogExecutionTime
    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )

    @Override
    public void print(Claim claim, Map<ClaimDocumentType, Document> documents) {
        requireNonNull(claim);
        List<Document> docs = new ArrayList<>(documents.values());
        docs.forEach(Objects::requireNonNull);

        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(
                docs,
                XEROX_TYPE_PARAMETER,
                wrapInMap(claim)
            )
        );
    }

    @Recover
    public void notifyStaffForBulkPrintFailure(
        RuntimeException exception,
        Claim claim,
        Map<ClaimDocumentType, Document> documents
    ) {
        Document defendantLetterDocument = documents.get(ClaimDocumentType.DEFENDANT_PIN_LETTER);
        Document sealedClaimDocument = documents.get(ClaimDocumentType.SEALED_CLAIM);
        bulkPrintStaffNotificationService.notifyFailedBulkPrint(
            defendantLetterDocument,
            sealedClaimDocument,
            claim
        );

        appInsights.trackEvent(BULK_PRINT_FAILED, REFERENCE_NUMBER, claim.getReferenceNumber());
        if (asyncEventProcessingEnabled) {
            throw exception;
        }
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, ADDITIONAL_DATA_LETTER_TYPE_VALUE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());
        return additionalData;
    }
}

package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    protected static final String FIRST_CONTACT_LETTER_TYPE = "first-contact-pack";
    protected static final String DIRECTION_ORDER_LETTER_TYPE = "direction-order-pack";
    protected static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";
    protected static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final AppInsights appInsights;
    private final BulkPrintStaffNotificationService bulkPrintStaffNotificationService;
    private final PDFServiceClient pdfServiceClient;
    @Value("${feature_toggles.async_event_operations_enabled:false}")
    private boolean asyncEventProcessingEnabled;

    @Autowired
    public BulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator,
        BulkPrintStaffNotificationService bulkPrintStaffNotificationService,
        AppInsights appInsights,
        PDFServiceClient pdfServiceClient
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
        this.appInsights = appInsights;
        this.bulkPrintStaffNotificationService = bulkPrintStaffNotificationService;
        this.pdfServiceClient = pdfServiceClient;
    }

    @LogExecutionTime
    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    @Override
    public void print(Claim claim, List<Printable> documents) {
        requireNonNull(claim);
        List<Document> docs = documents.stream()
            .filter(Objects::nonNull)
            .map(Printable::getDocument)
            .collect(Collectors.toList());

        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(
                docs,
                XEROX_TYPE_PARAMETER,
                wrapInFirstContactDetailsInMap(claim)
            )
        );
    }

    @Recover
    public void notifyStaffForBulkPrintFailure(
        RuntimeException exception,
        Claim claim,
        List<Printable> documents
    ) {
        bulkPrintStaffNotificationService.notifyFailedBulkPrint(
            documents,
            claim
        );
        appInsights.trackEvent(BULK_PRINT_FAILED, REFERENCE_NUMBER, claim.getReferenceNumber());
        if (asyncEventProcessingEnabled) {
            throw exception;
        }
    }

    @LogExecutionTime
    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    @Override
    public void printPdf(Claim claim, List<Printable> documents) {
        requireNonNull(claim);

        List<String> docs = documents.stream()
            .filter(Objects::nonNull)
            .map(Printable::getDocument)
            .map(this::readDocuments)
            .collect(Collectors.toList());

        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new LetterWithPdfsRequest(
                docs,
                XEROX_TYPE_PARAMETER,
                wrapInOrderDetailsInMap(claim)
            )
        );
    }

    private String readDocuments(Document document) {
        if (document.values.isEmpty()) {
            return document.template;
        }

        byte[] html = pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        return Base64.getEncoder().encodeToString(html);
    }

    private static Map<String, Object> wrapInFirstContactDetailsInMap(Claim claim) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, FIRST_CONTACT_LETTER_TYPE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());
        return additionalData;
    }

    private Map<String, Object> wrapInOrderDetailsInMap(Claim claim) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, DIRECTION_ORDER_LETTER_TYPE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());
        return additionalData;
    }
}

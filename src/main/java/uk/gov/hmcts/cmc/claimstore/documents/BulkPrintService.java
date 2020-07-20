package uk.gov.hmcts.cmc.claimstore.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.GENERAL_LETTER_TYPE;

@Service
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService implements PrintService {
    private final Logger logger = LoggerFactory.getLogger(BulkPrintService.class);

    /* This is configured on Xerox end so they know its us printing and controls things
     like paper quality and resolution */
    public static final String XEROX_TYPE_PARAMETER = "CMC001";

    protected static final String ADDITIONAL_DATA_LETTER_TYPE_KEY = "letterType";
    protected static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";
    protected static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final AppInsights appInsights;
    private final BulkPrintStaffNotificationService bulkPrintStaffNotificationService;
    private final PDFServiceClient pdfServiceClient;

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
    public BulkPrintDetails printHtmlLetter(
        Claim claim,
        List<Printable> documents,
        BulkPrintRequestType letterType,
        String authorisation
    ) {
        requireNonNull(claim);
        List<Document> docs = documents.stream()
            .filter(Objects::nonNull)
            .map(Printable::getDocument)
            .collect(Collectors.toList());

        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(
                docs,
                XEROX_TYPE_PARAMETER,
                wrapInDetailsInMap(claim, letterType)
            )
        );

        logger.info(letterType.logInfo,
            sendLetterResponse.letterId,
            claim.getReferenceNumber()
        );

        return BulkPrintDetails.builder()
            .printRequestId(sendLetterResponse.letterId.toString())
            .printRequestType(letterType.printRequestType)
            .printRequestedAt(LocalDate.now())
            .build();
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
        throw exception;
    }

    @LogExecutionTime
    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    @Override
    public BulkPrintDetails printPdf(
        Claim claim,
        List<Printable> documents,
        BulkPrintRequestType letterType,
        String authorisation
    ) {
        requireNonNull(claim);
        String info = "";
        if (letterType.equals(DIRECTION_ORDER_LETTER_TYPE)) {
            info = "Direction order pack letter {} created for letter type {} claim reference {}";
        }
        if (letterType.equals(GENERAL_LETTER_TYPE)) {
            info = "General Letter {} created for letter type {} claim reference {}";
        }
        if (letterType.equals(BULK_PRINT_TRANSFER_TYPE)) {
            info = "Bulk print request {} created for request    type {} claim reference {}";
        }

        List<String> docs = documents.stream()
            .filter(Objects::nonNull)
            .map(Printable::getDocument)
            .map(this::readDocuments)
            .collect(Collectors.toList());

        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new LetterWithPdfsRequest(
                docs,
                XEROX_TYPE_PARAMETER,
                wrapInDetailsInMap(claim, letterType)
            )
        );

        logger.info(info,
            sendLetterResponse.letterId,
            letterType,
            claim.getReferenceNumber()
        );

        return BulkPrintDetails.builder()
            .printRequestId(sendLetterResponse.letterId.toString())
            .printRequestType(letterType.printRequestType)
            .printRequestedAt(LocalDate.now())
            .build();
    }

    private String readDocuments(Document document) {
        if (document.values.isEmpty()) {
            // This scenario is only valid for direction order or general letter (generated by DOCMOSIS)
            // as in all other cases we generates pdf
            return document.template;
        }

        byte[] html = pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        return Base64.getEncoder().encodeToString(html);
    }

    private static Map<String, Object> wrapInDetailsInMap(Claim claim, BulkPrintRequestType letterType) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, letterType.value);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());
        return additionalData;
    }
}

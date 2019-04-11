package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;

@Service
public class BulkPrintService {

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

    public BulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator,
        BulkPrintStaffNotificationService bulkPrintStaffNotificationService,
        AppInsights appInsights
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
        this.bulkPrintStaffNotificationService = bulkPrintStaffNotificationService;
        this.appInsights = appInsights;
    }

    @LogExecutionTime
    @Retryable(
        value = {HttpClientErrorException.class, HttpServerErrorException.class},
        backoff = @Backoff(delay = 200)
    )
    public void print(Claim claim, Document defendantLetterDocument, Document sealedClaimDocument) {
        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(Arrays.asList(defendantLetterDocument, sealedClaimDocument),
                XEROX_TYPE_PARAMETER, wrapInMap(claim)
            )
        );
    }

    @Recover
    public void notifyStaffForBulkPrintFailure(
        RuntimeException exception,
        Claim claim,
        Document defendantLetterDocument,
        Document sealedClaimDocument
    ) {
        bulkPrintStaffNotificationService.notifyFailedBulkPrint(
            defendantLetterDocument,
            sealedClaimDocument,
            claim
        );

        appInsights.trackEvent(BULK_PRINT_FAILED, "referenceNumber", claim.getReferenceNumber());
    }

    private static Map<String, Object> wrapInMap(Claim claim) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, ADDITIONAL_DATA_LETTER_TYPE_VALUE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());
        return additionalData;
    }
}

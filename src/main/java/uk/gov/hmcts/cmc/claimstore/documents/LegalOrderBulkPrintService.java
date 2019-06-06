package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;

@Service
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class LegalOrderBulkPrintService implements PrintService {

    /* This is configured on Xerox end so they know its us printing and controls things
     like paper quality and resolution */
    private static final String XEROX_TYPE_PARAMETER = "CMC001";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final AppInsights appInsights;

    @Autowired
    public LegalOrderBulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator,
        AppInsights appInsights
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
        this.appInsights = appInsights;
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
                Collections.emptyMap()
            )
        );
    }

    @Recover
    public void showErrorForBulkPrintFailure(
        RuntimeException exception,
        Claim claim,
        Map<ClaimDocumentType, Document> documents
    ) {
        appInsights.trackEvent(BULK_PRINT_FAILED, REFERENCE_NUMBER, claim.getReferenceNumber());
        throw exception;
    }
}

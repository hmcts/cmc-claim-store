package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

@Service
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService {

    /* This is configured on Xerox end so they know its us printing and controls things
     like paper quality and resolution */
    protected static final String XEROX_TYPE_PARAMETER = "CMC001";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final BulkPrintStaffNotificationService bulkPrintStaffNotificationService;

    public BulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator,
        BulkPrintStaffNotificationService bulkPrintStaffNotificationService
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
        this.bulkPrintStaffNotificationService = bulkPrintStaffNotificationService;
    }

    @EventListener
    @Retryable(
        value = {HttpClientErrorException.class, HttpServerErrorException.class},
        backoff = @Backoff(delay = 200)
    )
    public void print(DocumentReadyToPrintEvent event) {
        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(event.getDocuments(), XEROX_TYPE_PARAMETER)
        );
    }

    @Recover
    public void notifyStaffForBulkPrintFailure(DocumentReadyToPrintEvent event) {
        bulkPrintStaffNotificationService.notifyFailedBulkPrint(event.getDocuments(), event.getClaim());
    }
}

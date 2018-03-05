package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

@Service
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService {

    private static final String XEROX_TYPE_PARAMETER = "CMC001";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    public BulkPrintService(
        SendLetterApi sendLetterApi,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.sendLetterApi = sendLetterApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    @EventListener
    public void uploadIntoDocumentManagementStore(DocumentReadyToPrintEvent event) {
        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(event.getDocuments(), XEROX_TYPE_PARAMETER)
        );
    }
}

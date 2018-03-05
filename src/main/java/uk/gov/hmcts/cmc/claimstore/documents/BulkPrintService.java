package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import static java.util.Arrays.asList;

@Service
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

    public void print(Document... documents) {
        sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new Letter(asList(documents), XEROX_TYPE_PARAMETER)
        );
    }
}

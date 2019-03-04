package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DocumentUploadHandlerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;
    private static final DefendantResponseEvent RESPONSE_EVENT = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE,
        AUTHORISATION
    );

    private static final DefendantResponseEvent RESPONSE_EVENT_WITHOUT_RESPONSE = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_NO_RESPONSE,
        AUTHORISATION
    );
    private DocumentUploadHandler documentUploadHandler;

    @Before
    public void setUp() {
        documentUploadHandler = new DocumentUploadHandler(publisher,
                            defendantResponseReceiptService);
    }

    @Test
    public void notifyDefendantResponseTriggersDocumentUploadEvent() {
        documentUploadHandler.uploadDocument(RESPONSE_EVENT);
        verify(publisher).publishEvent(any(DocumentUploadEvent.class));
    }

    @Test
    public void uploadDocumentThrowsExceptionWhenResponseNotPresent() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Response must be present");
        documentUploadHandler.uploadDocument(RESPONSE_EVENT_WITHOUT_RESPONSE);
    }

    @Test
    public void uploadDocumentThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must be present");
        documentUploadHandler.uploadDocument(new DefendantResponseEvent(null, AUTHORISATION));
    }
}

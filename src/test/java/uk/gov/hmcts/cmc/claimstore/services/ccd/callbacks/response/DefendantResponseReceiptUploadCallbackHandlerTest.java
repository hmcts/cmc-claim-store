package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_RESPONSE_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@RunWith(MockitoJUnitRunner.class)
@DisplayName("Defendant response upload")
public class DefendantResponseReceiptUploadCallbackHandlerTest {
    @InjectMocks
    private DefendantResponseReceiptUploadCallbackHandler defendantResponseReceiptUploadCallbackHandler;

    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private CallbackParams callbackParams;

    private static final String LAST_EVENT_TRIGGERED = "DefendantResponseReceiptUpload";

    @Before
    public void before() {
        this.defendantResponseReceiptUploadCallbackHandler =
            new DefendantResponseReceiptUploadCallbackHandler(
                caseDetailsConverter,
                defendantResponseReceiptService
            );
    }

    @Test
    public void shouldUploadDefendantResponseReceiptUpload() {

        Claim claim = SampleClaim.builder()
            .withLastEventTriggered(LAST_EVENT_TRIGGERED).build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE_UPLOAD.getValue())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        defendantResponseReceiptUploadCallbackHandler.handle(callbackParams);

        Mockito.verify(defendantResponseReceiptService, atLeastOnce()).createPdf(claim);
    }

    @Test
    public void shouldNotUploadDefendantResponseReceiptUploadIfDocumentIsPresent() {

        ClaimDocumentCollection documentCollection = new ClaimDocumentCollection();
        documentCollection.addClaimDocument(ClaimDocument.builder()
            .documentType(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT).build());

        Claim claim = SampleClaim.builder().withClaimDocumentCollection(documentCollection)
            .withLastEventTriggered(LAST_EVENT_TRIGGERED).build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(DEFENDANT_RESPONSE_UPLOAD.getValue())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        defendantResponseReceiptUploadCallbackHandler.handle(callbackParams);

        Mockito.verify(defendantResponseReceiptService, never()).createPdf(claim);
    }

}

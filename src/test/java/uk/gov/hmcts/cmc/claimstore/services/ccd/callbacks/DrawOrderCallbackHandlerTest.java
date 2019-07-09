package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.adapter.util.SampleData;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class DrawOrderCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");

    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Clock clock;
    @Mock
    private OrderDrawnNotificationService orderDrawnNotificationService;

    private CallbackParams callbackParams;

    private CallbackRequest callbackRequest;

    private DrawOrderCallbackHandler drawOrderCallbackHandler;

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl("http://bla.test")
        .build();

    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                .build())
            .build();

    @Before
    public void setUp() {
        drawOrderCallbackHandler = new DrawOrderCallbackHandler(
            clock,
            jsonMapper,
            orderDrawnNotificationService,
            caseDetailsConverter);
        when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        callbackRequest = CallbackRequest
            .builder()
            .eventId(DRAW_ORDER.getValue())
            .caseDetails(CaseDetails.builder()
                .id(3L)
                .data(Collections.emptyMap())
                .build())
            .build();
    }

    @Test
    public void shouldAddDraftDocumentToEmptyCaseDocumentsOnEventStart() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setOrderGenerationData(
            CCDOrderGenerationData.builder()
                .draftOrderDoc(DOCUMENT)
                .build()
        );
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class))
            .thenReturn(ccdCase);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("caseDocuments", ImmutableList.of(CLAIM_DOCUMENT))
        );
    }

    @Test
    public void shouldAddDraftDocumentToExistingCaseDocumentsOnEventStart() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setOrderGenerationData(
            CCDOrderGenerationData.builder()
                .draftOrderDoc(DOCUMENT)
                .build()
        );
        CCDCollectionElement<CCDClaimDocument> existingDocument =
            CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(CCDDocument
                        .builder()
                        .documentUrl("http://anotherbla.test")
                        .build())
                    .build())
                .build();
        ccdCase.setCaseDocuments(ImmutableList.of(existingDocument));
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class))
            .thenReturn(ccdCase);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("caseDocuments", ImmutableList.of(existingDocument, CLAIM_DOCUMENT))
        );
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfDraftOrderIsNotPresent() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class))
            .thenReturn(ccdCase);

        drawOrderCallbackHandler
            .handle(callbackParams);
    }

    @Test
    public void shouldNotifyPartiesOnEventSubmitted() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .build();
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        drawOrderCallbackHandler
            .handle(callbackParams);

        verify(orderDrawnNotificationService).notifyDefendant(claim);
        verify(orderDrawnNotificationService).notifyClaimant(claim);
    }
}

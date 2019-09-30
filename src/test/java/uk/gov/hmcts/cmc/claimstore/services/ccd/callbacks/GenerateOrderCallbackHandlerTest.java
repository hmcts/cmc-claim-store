package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class GenerateOrderCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final LocalDate DEADLINE = LocalDate.parse("2018-11-16");
    private static final String DOC_URL = "http://success.test";

    @Mock
    private LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocAssemblyService docAssemblyService;

    private CallbackRequest callbackRequest;
    private GenerateOrderCallbackHandler generateOrderCallbackHandler;
    private CCDCase ccdCase;

    @Before
    public void setUp() {
        generateOrderCallbackHandler = new GenerateOrderCallbackHandler(
            legalOrderGenerationDeadlinesCalculator,
            jsonMapper,
            caseDetailsConverter,
            docAssemblyService
        );

        ReflectionTestUtils.setField(generateOrderCallbackHandler, "templateId", "testTemplateId");
        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        Claim claim =
            SampleClaim.builder()
                .withResponse(
                    FullDefenceResponse.builder()
                        .directionsQuestionnaire(DirectionsQuestionnaire.builder()
                            .hearingLocation(
                                HearingLocation.builder()
                                    .courtName("Defendant Preferred Court")
                                    .build()
                            )
                            .build()).build()
                ).build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
            .thenReturn(DEADLINE);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(GENERATE_ORDER.getValue())
            .build();
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfNobodyObjectsCourt() {
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .claimantResponse(CCDResponseRejection.builder()
                            .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder().build())
                            .build())
                        .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder().build())
                        .build())
                    .build()
            ));
        ccdCase.setDirectionOrderData(SampleData.getCCDOrderGenerationData());

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generateOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", "Defendant Preferred Court"),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfOtherDirectionHeaderIsNull() {
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData().toBuilder()
            .hearingCourt(null)
            .build();

        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .claimantResponse(CCDResponseRejection.builder()
                            .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder().build())
                            .build())
                        .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder().build())
                        .build())
                    .build()
            ));
        ccdCase.setDirectionOrderData(ccdOrderGenerationData);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generateOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", "Defendant Preferred Court"),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null)
        ).doesNotContain(
            entry("hearingCourt", null)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfClaimantsObjectsCourt() {
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ));
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generateOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", "Defendant Preferred Court"),
            entry("newRequestedCourt", "Claimant Court"),
            entry("preferredCourtObjectingParty", "Res_CLAIMANT"),
            entry("preferredCourtObjectingReason", "As a claimant I like this court more")
        ).doesNotContain(
            entry("otherDirectionHeaders", "HEADER_UPLOAD")
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfDefendantObjectsCourt() {
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQ())
                    .build()
            ));
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generateOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", "Defendant Preferred Court"),
            entry("newRequestedCourt", "Defendant Court"),
            entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
            entry("preferredCourtObjectingReason", "As a defendant I like this court more")
        );
    }

    @Test
    public void shouldGenerateDocumentOnMidEvent() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setDirectionOrderData(SampleData.getCCDOrderGenerationData());
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .caseDetailsBefore(CaseDetails.builder().data(Collections.emptyMap()).build())
            .build();

        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(docAssemblyService.createOrder(eq(ccdCase), eq(BEARER_TOKEN)))
            .thenReturn(docAssemblyResponse);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) generateOrderCallbackHandler
                .handle(callbackParams);

        CCDDocument document = CCDDocument.builder().documentUrl(DOC_URL).build();
        assertThat(response.getData()).contains(entry("draftOrderDoc", document));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfClaimantResponseIsNotPresent() {
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData().toBuilder()
            .otherDirections(null)
            .build();

        ccdCase.setDirectionOrderData(ccdOrderGenerationData);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        generateOrderCallbackHandler
            .handle(callbackParams);
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        generateOrderCallbackHandler
            .handle(callbackParams);
    }
}

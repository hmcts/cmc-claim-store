package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.GenerateOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_JUDGES_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@RunWith(MockitoJUnitRunner.class)
public class DrawJudgeOrderCallbackHandlerTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final LocalDate DEADLINE = LocalDate.parse("2018-11-16");
    private static final String DEFENDANT_PREFERRED_COURT = "Defendant Preferred Court";

    private final CaseDetails caseDetails = CaseDetails.builder().id(3L).data(Collections.emptyMap()).build();

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();

    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                .build())
            .build();

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Clock clock;
    @Mock
    private OrderDrawnNotificationService orderDrawnNotificationService;
    @Mock
    private LegalOrderService legalOrderService;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private DirectionsQuestionnaireService directionsQuestionnaireService;
    @Mock
    private AppInsights appInsights;

    @Mock
    private LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;

    @Mock
    private GenerateOrderRule generateOrderRule;

    @Mock
    private PilotCourtService pilotCourtService;

    @Mock
    private DirectionOrderService directionOrderService;

    private CallbackParams callbackParams;

    private CallbackRequest callbackRequest;

    private DrawJudgeOrderCallbackHandler drawJudgeOrderCallbackHandler;

    @Before
    public void setUp() {
        OrderCreator orderCreator = new OrderCreator(legalOrderGenerationDeadlinesCalculator, caseDetailsConverter,
            docAssemblyService, generateOrderRule, directionsQuestionnaireService, pilotCourtService);

        OrderPostProcessor orderPostProcessor = new OrderPostProcessor(clock, orderDrawnNotificationService,
            caseDetailsConverter, legalOrderService, appInsights, directionOrderService);

        drawJudgeOrderCallbackHandler = new DrawJudgeOrderCallbackHandler(orderCreator, orderPostProcessor);

        Claim claim =
            SampleClaim.builder()
                .withFeatures(ImmutableList.of(DQ_FLAG.getValue()))
                .withResponse(
                    FullDefenceResponse.builder()
                        .directionsQuestionnaire(DirectionsQuestionnaire.builder()
                            .hearingLocation(
                                HearingLocation.builder()
                                    .courtName(DEFENDANT_PREFERRED_COURT)
                                    .build()
                            )
                            .build()).build()
                ).build();

        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines()).thenReturn(DEADLINE);

        when(directionsQuestionnaireService.getPreferredCourt(eq(claim))).thenReturn(DEFENDANT_PREFERRED_COURT);

        when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(UTC_ZONE)).thenReturn(clock);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(DRAW_JUDGES_ORDER.getValue())
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void shouldAddDraftDocumentToCaseDocumentsOnEventStart() {
        ImmutableMap<String, Object> data = ImmutableMap.of("data", "existingData",
            "caseDocuments", ImmutableList.of(CLAIM_DOCUMENT));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(3L)
            .data(data)
            .build();

        callbackRequest = CallbackRequest
            .builder()
            .eventId(DRAW_JUDGES_ORDER.getValue())
            .caseDetails(caseDetails)
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
            .draftOrderDoc(DOCUMENT)
            .hearingCourt(PilotCourtService.OTHER_COURT_ID)
            .directionOrder(CCDDirectionOrder.builder()
                .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
                .hearingCourtAddress(SampleData.getHearingCourtAddress())
                .build())
            .hearingCourt(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
            .build();

        when(directionOrderService.getHearingCourt(any())).thenReturn(HearingCourt.builder().build());

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
            .thenReturn(ImmutableMap.<String, Object>builder()
                .put("data", "existingData")
                .put("caseDocuments", ImmutableList.of(CLAIM_DOCUMENT, DOCUMENT))
                .build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler.handle(callbackParams);

        Map<String, Object> responseData = response.getData();
        assertThat(responseData).contains(
            entry("caseDocuments", ImmutableList.of(CLAIM_DOCUMENT, DOCUMENT)),
            entry("data", "existingData")
        );
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfDraftOrderIsNotPresent() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        drawJudgeOrderCallbackHandler.handle(callbackParams);
    }

    @Test
    public void shouldNotifyPartiesAndPrintDocumentsOnEventSubmitted() {
        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CCDCollectionElement<CCDClaimDocument> existingDocument =
            CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(CCDDocument
                        .builder()
                        .documentUrl("http://anotherbla.test")
                        .build())
                    .build())
                .build();

        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
            .draftOrderDoc(DOCUMENT)
            .directionOrder(CCDDirectionOrder.builder()
                .hearingCourtName(SampleData.MANCHESTER_CIVIL_JUSTICE_CENTRE_CIVIL_AND_FAMILY_COURTS)
                .hearingCourtAddress(SampleData.getHearingCourtAddress())
                .build())
            .caseDocuments(ImmutableList.of(existingDocument))
            .build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        drawJudgeOrderCallbackHandler.handle(callbackParams);

        verify(appInsights).trackEvent(AppInsightsEvent.DRAW_JUDGES_ORDER, AppInsights.REFERENCE_NUMBER,
                ccdCase.getPreviousServiceCaseReference());
        verify(orderDrawnNotificationService).notifyDefendant(claim);
        verify(orderDrawnNotificationService).notifyClaimant(claim);
        verify(legalOrderService).print(
            BEARER_TOKEN,
            claim,
            DOCUMENT);
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfExpertReportsAreProvided() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .claimantResponse(CCDResponseRejection.builder()
                            .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder()
                                .expertRequired(YES)
                                .expertReports(ImmutableList.of(CCDCollectionElement.<CCDExpertReport>builder()
                                    .value(CCDExpertReport.builder().expertName("expertName")
                                        .expertReportDate(LocalDate.now())
                                        .build())
                                    .build()))
                                .build())
                            .build())
                        .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder()
                            .expertRequired(YES)
                            .expertReports(ImmutableList.of(CCDCollectionElement.<CCDExpertReport>builder()
                                .value(CCDExpertReport.builder().expertName("expertName")
                                    .expertReportDate(LocalDate.now())
                                    .build())
                                .build()))
                            .build())
                        .build())
                    .build()
            ));

        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfNobodyObjectsCourt() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .claimantResponse(CCDResponseRejection.builder()
                            .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder()
                                .expertRequired(YES)
                                .permissionForExpert(YES)
                                .expertEvidenceToExamine("Some Evidence")
                                .build())
                            .build())
                        .directionsQuestionnaire(CCDDirectionsQuestionnaire.builder()
                            .expertRequired(YES)
                            .permissionForExpert(YES)
                            .expertEvidenceToExamine("Some Evidence")
                            .build())
                        .build())
                    .build()
            ));
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfOtherDirectionHeaderIsNull() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());

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
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", NO),
            entry("expertReportPermissionPartyAskedByDefendant", NO)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfClaimantsObjectsCourt() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ));
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", "Claimant Court"),
            entry("preferredCourtObjectingParty", "Res_CLAIMANT"),
            entry("preferredCourtObjectingReason", "As a claimant I like this court more"),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", NO)
        ).doesNotContain(
            entry("otherDirectionHeaders", "HEADER_UPLOAD")
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfDefendantObjectsCourt() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQ())
                    .build()
            ));
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("docUploadForParty", "BOTH"),
            entry("eyewitnessUploadDeadline", DEADLINE),
            entry("eyewitnessUploadForParty", "BOTH"),
            entry("paperDetermination", "NO"),
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", "Defendant Court"),
            entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
            entry("preferredCourtObjectingReason", "As a defendant I like this court more"),
            entry("expertReportPermissionPartyAskedByDefendant", NO),
            entry("expertReportPermissionPartyAskedByClaimant", NO)
        );
    }

    @Test
    public void shouldGenerateDocumentOnMidEvent() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .caseDetailsBefore(CaseDetails.builder().data(Collections.emptyMap()).build())
            .build();

        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOCUMENT_URL);
        when(docAssemblyService.createOrder(eq(ccdCase), eq(BEARER_TOKEN)))
            .thenReturn(docAssemblyResponse);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) drawJudgeOrderCallbackHandler
                .handle(callbackParams);

        CCDDocument document = CCDDocument.builder().documentUrl(DOCUMENT_URL).build();
        assertThat(response.getData()).contains(entry("draftOrderDoc", document));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfClaimantResponseIsNotPresent() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = ccdCase.toBuilder().otherDirections(null).build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        drawJudgeOrderCallbackHandler
            .handle(callbackParams);
    }
}

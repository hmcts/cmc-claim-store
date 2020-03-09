package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
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
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackVersion;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.GenerateOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_JUDGES_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EXPERT_REPORT_PERMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.OTHER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOtherDirectionHeaderType.UPLOAD;

@ExtendWith(MockitoExtension.class)
class DrawJudgeOrderCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String DOC_URL = "http://success.test";
    private static final String DEFENDANT_PREFERRED_COURT = "Defendant Preferred Court";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";

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
    private LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private AppInsights appInsights;
    @Mock
    private DirectionsQuestionnaireService directionsQuestionnaireService;
    @Mock
    private PilotCourtService pilotCourtService;
    @Mock
    private Clock clock;
    @Mock
    private OrderDrawnNotificationService orderDrawnNotificationService;
    @Mock
    private LegalOrderService legalOrderService;
    @Mock
    private DirectionOrderService directionOrderService;

    private CallbackRequest callbackRequest;
    private DrawJudgeOrderCallbackHandler drawJudgeOrderCallbackHandler;

    @BeforeEach
    void setUp() {
        OrderCreator orderCreator = new OrderCreator(legalOrderGenerationDeadlinesCalculator, caseDetailsConverter,
            docAssemblyService, new GenerateOrderRule(), directionsQuestionnaireService, pilotCourtService);

        OrderPostProcessor orderPostProcessor = new OrderPostProcessor(clock, orderDrawnNotificationService,
            caseDetailsConverter, legalOrderService, appInsights, directionOrderService);

        drawJudgeOrderCallbackHandler = new DrawJudgeOrderCallbackHandler(orderCreator, orderPostProcessor);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(DRAW_JUDGES_ORDER.getValue())
            .build();
    }

    @Nested
    @DisplayName("Mid tests")
    class MidTests {

        @Test
        void shouldGenerateDocumentOnMidEvent() {
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
            when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
            when(docAssemblyService.createOrder(eq(ccdCase), eq(BEARER_TOKEN)))
                .thenReturn(docAssemblyResponse);

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(callbackRequest)
                .version(CallbackVersion.V_2)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) drawJudgeOrderCallbackHandler
                    .handle(callbackParams);

            CCDDocument document = CCDDocument.builder().documentUrl(DOC_URL).build();
            assertThat(response.getData()).contains(entry("draftOrderDoc", document));
        }
    }

    @Nested
    @DisplayName("Submitted tests")
    class SubmittedTests {

        @Test
        void shouldNotifyPartiesAndPrintDocumentsOnEventSubmitted() {
            CallbackParams callbackParams = CallbackParams.builder()
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
    }

    @Nested
    @DisplayName("About to Submit tests")
    class AboutToSubmitTests {

        private CallbackParams callbackParams;

        private CCDAddress address;

        private final String courtName = "Birmingham Court";
        private HearingCourt hearingCourt;

        @BeforeEach
        void setUp() {
            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

            address = CCDAddress.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .addressLine3("line3")
                .postCode("SW1P4BB")
                .postTown("Birmingham")
                .build();

            hearingCourt = HearingCourt.builder().name(courtName).address(address).build();
        }

        @Test
        void shouldAddDraftDocumentToCaseDocumentsOnEventStart() {
            when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);

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

            CallbackParams callbackParams = CallbackParams.builder()
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

        @Test
        void shouldThrowIfDraftOrderIsNotPresent() {
            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

            CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            Assertions.assertThrows(CallbackException.class, () ->
                drawJudgeOrderCallbackHandler.handle(callbackParams));
        }

        @Test
        void shouldPersistHearingCourt() {
            CCDCase ccdCase = CCDCase.builder()
                .draftOrderDoc(DOCUMENT)
                .caseDocuments(Collections.emptyList())
                .hearingCourt("birmingham")
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            when(directionOrderService.getHearingCourt(any())).thenReturn(hearingCourt);

            when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);

            drawJudgeOrderCallbackHandler.handle(callbackParams);

            ArgumentCaptor<CCDCase> argument = ArgumentCaptor.forClass(CCDCase.class);
            verify(caseDetailsConverter).convertToMap(argument.capture());
            CCDDirectionOrder directionOrder = argument.getValue().getDirectionOrder();

            assertThat(directionOrder.getHearingCourtName()).isEqualTo(courtName);
            assertThat(directionOrder.getHearingCourtAddress()).isEqualTo(address);
        }

        @Test
        void shouldClearHearingCourtField() {
            CCDCase ccdCase = CCDCase.builder()
                .draftOrderDoc(DOCUMENT)
                .caseDocuments(Collections.emptyList())
                .hearingCourt("BIRMINGHAM")
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);

            when(directionOrderService.getHearingCourt(any())).thenReturn(hearingCourt);

            drawJudgeOrderCallbackHandler.handle(callbackParams);

            ArgumentCaptor<CCDCase> argument = ArgumentCaptor.forClass(CCDCase.class);
            verify(caseDetailsConverter).convertToMap(argument.capture());
            CCDCase returnedValue = argument.getValue();

            assertThat(returnedValue.getHearingCourt()).isNull();
        }

    }

    @Nested
    @DisplayName("About To Start tests")
    class AboutToStartTests {

        @Nested
        @DisplayName("V1 tests")
        class V1Tests {

            @Nested
            @DisplayName("Test field population")
            class FieldPopulationTests {
                CallbackParams callbackParams;
                CCDCase ccdCase;

                @BeforeEach
                void setUp() {
                    ccdCase = CCDCase.builder()
                        .respondents(ImmutableList.of(
                            CCDCollectionElement.<CCDRespondent>builder()
                                .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                                .build()
                        ))
                        .build();
                    when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
                    when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

                    callbackParams = CallbackParams.builder()
                        .type(CallbackType.ABOUT_TO_START)
                        .request(callbackRequest)
                        .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                        .build();
                }

                @Test
                void shouldUseDefaultDirectionsListWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("directionList",
                        ImmutableList.of(CCDOrderDirectionType.DOCUMENTS, CCDOrderDirectionType.EYEWITNESS)));
                }

                @Test
                void shouldUseSavedDirectionsListWhenExistingCcdCase() {
                    ccdCase.setDirectionList(ImmutableList.of(CCDOrderDirectionType.DOCUMENTS));

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("directionList",
                        ImmutableList.of(CCDOrderDirectionType.DOCUMENTS)));
                }

                @Test
                void shouldUseDefaultDocUploadDeadlineWhenNewCcdCase() {
                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadDeadline", defaultDeadline));
                }

                @Test
                void shouldUseSavedDocUploadDeadlineWhenExistingCcdCase() {
                    LocalDate expectedDeadline = LocalDate.now().plusDays(5);
                    ccdCase.setDocUploadDeadline(expectedDeadline);

                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadDeadline", expectedDeadline));
                }

                @Test
                void shouldUseDefaultEyewitnessUploadDeadlineWhenNewCcdCase() {
                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("eyewitnessUploadDeadline", defaultDeadline));
                }

                @Test
                void shouldUseSavedEyewitnessUploadDeadlineWhenExistingCcdCase() {
                    LocalDate expectedDeadline = LocalDate.now().plusDays(5);
                    ccdCase.setEyewitnessUploadDeadline(expectedDeadline);

                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("eyewitnessUploadDeadline", expectedDeadline));
                }

                @Test
                void shouldUseDefaultDocUploadForPartyWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadForParty", CCDDirectionPartyType.BOTH));
                }

                @Test
                void shouldUseSavedDocUploadForPartyWhenExistingCcdCase() {
                    CCDDirectionPartyType docUploadForParty = CCDDirectionPartyType.DEFENDANT;

                    ccdCase.setDocUploadForParty(docUploadForParty);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadForParty", docUploadForParty));
                }

                @Test
                void shouldUseDefaultEyewitnessUploadForPartyWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("eyewitnessUploadForParty", CCDDirectionPartyType.BOTH));
                }

                @Test
                void shouldUseSavedEyewitnessUploadForPartyWhenExistingCcdCase() {
                    CCDDirectionPartyType eyewitnessUploadForParty = CCDDirectionPartyType.DEFENDANT;

                    ccdCase.setEyewitnessUploadForParty(eyewitnessUploadForParty);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("eyewitnessUploadForParty", eyewitnessUploadForParty));
                }

                @Test
                void shouldUseDefaultPaperDeterminationWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("paperDetermination", CCDYesNoOption.NO));
                }

                @Test
                void shouldUseSavedPaperDeterminationWhenExistingCcdCase() {
                    CCDYesNoOption paperDetermination = YES;

                    ccdCase.setPaperDetermination(paperDetermination);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("paperDetermination", paperDetermination));
                }

                @Test
                void shouldUseDefaultEstimatedHearingDurationWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("estimatedHearingDuration", null));
                }

                @Test
                void shouldUseSavedEstimatedHearingDurationWhenExistingCcdCase() {
                    CCDHearingDurationType estimatedHearingDuration = CCDHearingDurationType.TWO_HOURS;

                    ccdCase.setEstimatedHearingDuration(estimatedHearingDuration);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("estimatedHearingDuration", estimatedHearingDuration));
                }

                @Test
                void shouldUseDefaultOtherDirectionsWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("otherDirections", Collections.emptyList()));
                }

                @Test
                void shouldUseSavedOtherDirectionsWhenExistingCcdCase() {
                    List<CCDCollectionElement<CCDOrderDirection>> otherDirections = ImmutableList.of(
                        CCDOrderDirection.builder()
                            .sendBy(LocalDate.parse("2020-10-11"))
                            .directionComment("a direction")
                            .extraOrderDirection(OTHER)
                            .otherDirectionHeaders(UPLOAD)
                            .forParty(BOTH)
                            .build(),
                        CCDOrderDirection.builder()
                            .sendBy(LocalDate.parse("2020-10-11"))
                            .extraOrderDirection(EXPERT_REPORT_PERMISSION)
                            .forParty(BOTH)
                            .build()
                    )
                        .stream()
                        .map(e -> CCDCollectionElement.<CCDOrderDirection>builder().value(e).build())
                        .collect(Collectors.toList());

                    ccdCase.setOtherDirections(otherDirections);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("otherDirections", otherDirections));
                }

                @Test
                void shouldPrepopulateFieldsOnAboutToStartEventIfNobodyObjectsCourt() {
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
                    when(directionsQuestionnaireService.getPreferredCourt(any())).thenReturn(DEFENDANT_PREFERRED_COURT);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(
                        entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
                        entry("newRequestedCourt", null),
                        entry("preferredCourtObjectingParty", null),
                        entry("preferredCourtObjectingReason", null)
                    );
                }

                @Test
                void shouldPrepopulateFieldsOnAboutToStartEventIfClaimantsObjectsCourt() {
                    ccdCase.setRespondents(
                        ImmutableList.of(
                            CCDCollectionElement.<CCDRespondent>builder()
                                .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                                .build()
                        ));
                    when(directionsQuestionnaireService.getPreferredCourt(any())).thenReturn(DEFENDANT_PREFERRED_COURT);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(
                        entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
                        entry("newRequestedCourt", "Claimant Court"),
                        entry("preferredCourtObjectingParty", "Res_CLAIMANT"),
                        entry("preferredCourtObjectingReason", "As a claimant I like this court more"),
                        entry("otherDirections", Collections.emptyList())
                    );
                }

                @Test
                void shouldPrepopulateFieldsOnAboutToStartEventIfDefendantObjectsCourt() {
                    ccdCase.setRespondents(
                        ImmutableList.of(
                            CCDCollectionElement.<CCDRespondent>builder()
                                .value(SampleData.getIndividualRespondentWithDQ())
                                .build()
                        ));
                    when(directionsQuestionnaireService.getPreferredCourt(any())).thenReturn(DEFENDANT_PREFERRED_COURT);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(
                        entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
                        entry("newRequestedCourt", "Defendant Court"),
                        entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
                        entry("preferredCourtObjectingReason", "As a defendant I like this court more")
                    );
                }

                @Test
                void shouldUseDefaultGrantExpertReportPermissionWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("grantExpertReportPermission", CCDYesNoOption.NO));
                }

                @Test
                void shouldUseSavedGrantExpertReportPermissionWhenExistingCcdCase() {
                    CCDYesNoOption grantExpertReportPermission = YES;

                    ccdCase.setGrantExpertReportPermission(grantExpertReportPermission);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("grantExpertReportPermission", grantExpertReportPermission));
                }

                @Test
                void shouldUseDefaultExpertReportInstructionWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("expertReportInstruction", null));
                }

                @Test
                void shouldUseSavedExpertReportInstructionWhenExistingCcdCase() {
                    String expertReportInstruction = "EXPERT REPORT INSTRUCTION";

                    ccdCase.setExpertReportInstruction(expertReportInstruction);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("expertReportInstruction", expertReportInstruction));
                }

                @Test
                void shouldPopulateHearingCourt() {
                    CCDAddress address = CCDAddress.builder()
                        .addressLine1("line1")
                        .addressLine2("line2")
                        .addressLine3("line3")
                        .postCode("SW1P4BB")
                        .postTown("Birmingham")
                        .build();

                    String courtName = "Birmingham Court";
                    HearingCourt hearingCourt = HearingCourt.builder().name(courtName).address(address).build();
                    when(pilotCourtService.getPilotHearingCourts(any(), any()))
                        .thenReturn(ImmutableSet.of(hearingCourt));
                    String courtId = "BIRMINGHAM";
                    when(pilotCourtService.getPilotCourtId(any())).thenReturn(courtId);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        drawJudgeOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).containsKeys("hearingCourt");
                    assert response.getData().get("hearingCourt") instanceof Map;
                    Map<String, Object> hearingCourtMap = (Map<String, Object>)response.getData().get("hearingCourt");

                    assertThat(hearingCourtMap).containsKeys("list_items");
                    assertThat(hearingCourtMap).doesNotContainKeys("value");

                    List<Map<String, Object>> listItems =  (List<Map<String, Object>>)hearingCourtMap.get("list_items");
                    assertThat(listItems).contains(ImmutableMap.of("code", courtId, "label", hearingCourt.getName()));
                    assertThat(listItems).contains(ImmutableMap.of("code", PilotCourtService.OTHER_COURT_ID, "label",
                        "Other Court"));
                }
            }

            @Nested
            @DisplayName("Test exceptions")
            class Exceptions {

                @Test
                void shouldThrowIfClaimantResponseIsNotPresent() {
                    CCDCase ccdCase =
                        SampleData.addCCDOrderGenerationData(SampleData.getCCDCitizenCase(Collections.emptyList()))
                            .toBuilder()
                            .otherDirections(null)
                            .build();

                    when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

                    CallbackParams callbackParams = CallbackParams.builder()
                        .type(CallbackType.ABOUT_TO_START)
                        .request(callbackRequest)
                        .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                        .build();

                    when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

                    Assertions.assertThrows(IllegalStateException.class,
                        () -> drawJudgeOrderCallbackHandler.handle(callbackParams));
                }
            }
        }
    }
}

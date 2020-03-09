package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EXPERT_REPORT_PERMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.OTHER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOtherDirectionHeaderType.UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DRAFTED_BY_LEGAL_ADVISOR;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;

@ExtendWith(MockitoExtension.class)
public class GenerateOrderCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String DOC_URL = "http://success.test";
    private static final String DEFENDANT_PREFERRED_COURT = "Defendant Preferred Court";

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
    private GenerateOrderCallbackHandler generateOrderCallbackHandler;

    @BeforeEach
    void setUp() {
        OrderCreator orderCreator = new OrderCreator(legalOrderGenerationDeadlinesCalculator, caseDetailsConverter,
            docAssemblyService, new GenerateOrderRule(), directionsQuestionnaireService,
            pilotCourtService);

        OrderPostProcessor orderPostProcessor = new OrderPostProcessor(clock, orderDrawnNotificationService,
            caseDetailsConverter, legalOrderService, appInsights, directionOrderService);

        generateOrderCallbackHandler = new GenerateOrderCallbackHandler(orderCreator, orderPostProcessor,
            caseDetailsConverter, appInsights);

        ReflectionTestUtils.setField(generateOrderCallbackHandler, "templateId", "testTemplateId");

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(GENERATE_ORDER.getValue())
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
                (AboutToStartOrSubmitCallbackResponse) generateOrderCallbackHandler
                    .handle(callbackParams);

            CCDDocument document = CCDDocument.builder().documentUrl(DOC_URL).build();
            assertThat(response.getData()).contains(entry("draftOrderDoc", document));
        }
    }

    @Nested
    @DisplayName("Submitted tests")
    class SubmittedTests {

        private CallbackParams callbackParams;

        @BeforeEach
        void setUp() {
            callbackParams = CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

        }

        @Test
        void shouldRaiseAppInsight() {
            CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            generateOrderCallbackHandler.handle(callbackParams);

            verify(appInsights)
                .trackEvent(DRAFTED_BY_LEGAL_ADVISOR, REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference());
        }
    }

    @Nested
    @DisplayName("About to Submit tests")
    class AboutToSubmitTests {

        private CallbackParams callbackParams;

        private CCDAddress address;

        private final String courtName = "Birmingham Court";

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

            HearingCourt hearingCourt = HearingCourt.builder().name(courtName).address(address).build();
            when(directionOrderService.getHearingCourt(any())).thenReturn(hearingCourt);
        }

        @Test
        void shouldPersistHearingCourt() {
            CCDCase ccdCase = CCDCase.builder().hearingCourt("birmingham").build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            generateOrderCallbackHandler.handle(callbackParams);

            ArgumentCaptor<CCDCase> argument = ArgumentCaptor.forClass(CCDCase.class);
            verify(caseDetailsConverter).convertToMap(argument.capture());
            CCDCase returnedValue = argument.getValue();

            assertThat(returnedValue.getHearingCourtName()).isEqualTo(courtName);
            assertThat(returnedValue.getHearingCourtAddress()).isEqualTo(address);
        }

        @Test
        void shouldClearHearingCourtField() {
            CCDCase ccdCase = CCDCase.builder().hearingCourt("BIRMINGHAM").build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            generateOrderCallbackHandler.handle(callbackParams);

            ArgumentCaptor<CCDCase> argument = ArgumentCaptor.forClass(CCDCase.class);
            verify(caseDetailsConverter).convertToMap(argument.capture());
            CCDCase returnedValue = argument.getValue();

            assertThat(returnedValue.getHearingCourt()).isNull();
        }

        @Test
        void shouldRemoveOldExportReportFields() {
            CCDCase ccdCase = CCDCase.builder()
                .expertReportPermissionPartyGivenToClaimant(YES)
                .expertReportPermissionPartyGivenToDefendant(NO)
                .expertReportInstructionClaimant(Collections.emptyList())
                .expertReportInstructionDefendant(Collections.emptyList())
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            generateOrderCallbackHandler.handle(callbackParams);

            ArgumentCaptor<CCDCase> argument = ArgumentCaptor.forClass(CCDCase.class);
            verify(caseDetailsConverter).convertToMap(argument.capture());
            CCDCase returnedValue = argument.getValue();

            assertThat(returnedValue.getExpertReportPermissionPartyGivenToClaimant()).isNull();
            assertThat(returnedValue.getExpertReportPermissionPartyGivenToDefendant()).isNull();
            assertThat(returnedValue.getExpertReportInstructionClaimant()).isNull();
            assertThat(returnedValue.getExpertReportInstructionDefendant()).isNull();

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

                    callbackParams = CallbackParams.builder()
                        .type(CallbackType.ABOUT_TO_START)
                        .request(callbackRequest)
                        .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                        .build();
                }

                @Test
                void shouldUseDefaultDirectionsListWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("directionList",
                        ImmutableList.of(CCDOrderDirectionType.DOCUMENTS, CCDOrderDirectionType.EYEWITNESS)));
                }

                @Test
                void shouldUseSavedDirectionsListWhenExistingCcdCase() {
                    ccdCase.setDirectionList(ImmutableList.of(CCDOrderDirectionType.DOCUMENTS));

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("directionList",
                        ImmutableList.of(CCDOrderDirectionType.DOCUMENTS)));
                }

                @Test
                void shouldUseDefaultDocUploadDeadlineWhenNewCcdCase() {
                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadDeadline", expectedDeadline));
                }

                @Test
                void shouldUseDefaultEyewitnessUploadDeadlineWhenNewCcdCase() {
                    LocalDate defaultDeadline = LocalDate.now().plusDays(10);
                    when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
                        .thenReturn(defaultDeadline);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("eyewitnessUploadDeadline", expectedDeadline));
                }

                @Test
                void shouldUseDefaultDocUploadForPartyWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadForParty", CCDDirectionPartyType.BOTH));
                }

                @Test
                void shouldUseSavedDocUploadForPartyWhenExistingCcdCase() {
                    CCDDirectionPartyType docUploadForParty = CCDDirectionPartyType.DEFENDANT;

                    ccdCase.setDocUploadForParty(docUploadForParty);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("docUploadForParty", docUploadForParty));
                }

                @Test
                void shouldUseDefaultEyewitnessUploadForPartyWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("eyewitnessUploadForParty", CCDDirectionPartyType.BOTH));
                }

                @Test
                void shouldUseSavedEyewitnessUploadForPartyWhenExistingCcdCase() {
                    CCDDirectionPartyType eyewitnessUploadForParty = CCDDirectionPartyType.DEFENDANT;

                    ccdCase.setEyewitnessUploadForParty(eyewitnessUploadForParty);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("eyewitnessUploadForParty", eyewitnessUploadForParty));
                }

                @Test
                void shouldUseDefaultPaperDeterminationWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("paperDetermination", CCDYesNoOption.NO));
                }

                @Test
                void shouldUseSavedPaperDeterminationWhenExistingCcdCase() {
                    CCDYesNoOption paperDetermination = YES;

                    ccdCase.setPaperDetermination(paperDetermination);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("paperDetermination", paperDetermination));
                }

                @Test
                void shouldUseDefaultEstimatedHearingDurationWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("estimatedHearingDuration", null));
                }

                @Test
                void shouldUseSavedEstimatedHearingDurationWhenExistingCcdCase() {
                    CCDHearingDurationType estimatedHearingDuration = CCDHearingDurationType.TWO_HOURS;

                    ccdCase.setEstimatedHearingDuration(estimatedHearingDuration);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("estimatedHearingDuration", estimatedHearingDuration));
                }

                @Test
                void shouldUseDefaultOtherDirectionsWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(
                        entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
                        entry("newRequestedCourt", "Defendant Court"),
                        entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
                        entry("preferredCourtObjectingReason", "As a defendant I like this court more")
                    );
                }

                @Test
                void shouldNotPopulateHearingCourt() {

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).doesNotContainKeys("hearingCourt");
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
                        () -> generateOrderCallbackHandler.handle(callbackParams));
                }
            }
        }

        @Nested
        @DisplayName("V2 tests")
        class V2Tests {
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

                callbackParams = CallbackParams.builder()
                    .type(CallbackType.ABOUT_TO_START)
                    .version(CallbackVersion.V_2)
                    .request(callbackRequest)
                    .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                    .build();

            }

            @Nested
            @DisplayName("Test field population")
            class FieldPopulationTests {

                @BeforeEach
                void setUp() {
                    when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());
                }

                @Test
                void shouldUseDefaultGrantExpertReportPermissionWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("grantExpertReportPermission", CCDYesNoOption.NO));
                }

                @Test
                void shouldUseSavedGrantExpertReportPermissionWhenExistingCcdCase() {
                    CCDYesNoOption grantExpertReportPermission = YES;

                    ccdCase.setGrantExpertReportPermission(grantExpertReportPermission);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData())
                        .contains(entry("grantExpertReportPermission", grantExpertReportPermission));
                }

                @Test
                void shouldUseDefaultExpertReportInstructionWhenNewCcdCase() {
                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("expertReportInstruction", null));
                }

                @Test
                void shouldUseSavedExpertReportInstructionWhenExistingCcdCase() {
                    String expertReportInstruction = "EXPERT REPORT INSTRUCTION";

                    ccdCase.setExpertReportInstruction(expertReportInstruction);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

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
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).containsKeys("hearingCourt");
                    assert response.getData().get("hearingCourt") instanceof Map;
                    Map<String, Object> hearingCourtMap = (Map<String, Object>)response.getData().get("hearingCourt");

                    assertThat(hearingCourtMap).containsKeys("list_items");
                    assertThat(hearingCourtMap).doesNotContainKeys("value");

                    List<Map<String, Object>> listItems =  (List<Map<String, Object>>)hearingCourtMap.get("list_items");
                    assertThat(listItems).contains(ImmutableMap.of("code", courtId, "label", hearingCourt.getName()));
                    assertThat(listItems).doesNotContain(ImmutableMap.of("code", PilotCourtService.OTHER_COURT_ID,
                        "label", "Other Court"));
                }

                @Test
                void shouldPopulateSelectedHearingCourt() {
                    CCDAddress address = CCDAddress.builder()
                        .addressLine1("line1")
                        .addressLine2("line2")
                        .addressLine3("line3")
                        .postCode("SW1P4BB")
                        .postTown("Birmingham")
                        .build();

                    String courtName = "Birmingham Court";
                    ccdCase.setHearingCourtName(courtName);
                    HearingCourt hearingCourt = HearingCourt.builder().name(courtName).address(address).build();
                    when(pilotCourtService.getPilotHearingCourts(any(), any()))
                        .thenReturn(ImmutableSet.of(hearingCourt));

                    String courtId = "BIRMINGHAM";
                    when(pilotCourtService.getPilotCourtId(any())).thenReturn(courtId);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).containsKeys("hearingCourt");
                    assert response.getData().get("hearingCourt") instanceof Map;
                    Map<String, Object> hearingCourtMap = (Map<String, Object>)response.getData().get("hearingCourt");

                    assertThat(hearingCourtMap).containsKeys("value");

                    Map<String, Object> selectedValue = (Map<String, Object>)hearingCourtMap.get("value");
                    assertThat(selectedValue).containsExactly(entry("code", courtId),
                        entry("label", hearingCourt.getName()));
                }
            }

            @Nested
            @DisplayName("Migration tests")
            class MigrationTests {
                CallbackParams callbackParams;

                @BeforeEach
                void setUp() {
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
                                        ).build()
                                ).build()
                            ).build();
                    when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

                    callbackParams = CallbackParams.builder()
                        .type(CallbackType.ABOUT_TO_START)
                        .request(callbackRequest)
                        .version(CallbackVersion.V_2)
                        .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                        .build();

                    ccdCase.setRespondents(
                        ImmutableList.of(
                            CCDCollectionElement.<CCDRespondent>builder()
                                .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                                .build()
                        ));
                }

                @Test
                void shouldMigrateGrantExpertPermissionClaimant() {

                    ccdCase.setExpertReportPermissionPartyGivenToClaimant(YES);
                    ccdCase.setExpertReportPermissionPartyGivenToDefendant(NO);
                    List<CCDCollectionElement<String>> instructions = Stream.of("Instruction 1", "Instruction 2")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionClaimant(instructions);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler
                            .handle(callbackParams);

                    assertThat(response.getData()).contains(entry("grantExpertReportPermission", YES));
                    assertThat(response.getData()).contains(
                        entry("expertReportInstruction",
                            StringUtils.join(ImmutableList.of("Instruction 1", "Instruction 2"), ", "))
                    );
                }

                @Test
                void shouldMigrateGrantExpertPermissionDefendant() {
                    ccdCase.setExpertReportPermissionPartyGivenToClaimant(NO);
                    ccdCase.setExpertReportPermissionPartyGivenToDefendant(YES);
                    List<CCDCollectionElement<String>> instructions = Stream.of("Instruction 1", "Instruction 2")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionDefendant(instructions);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("grantExpertReportPermission", YES));
                    assertThat(response.getData()).contains(
                        entry("expertReportInstruction",
                            StringUtils.join(ImmutableList.of("Instruction 1", "Instruction 2"), ", "))
                    );
                }

                @Test
                void shouldMigrateGrantExpertPermissionBoth() {
                    ccdCase.setExpertReportPermissionPartyGivenToClaimant(YES);
                    ccdCase.setExpertReportPermissionPartyGivenToDefendant(YES);
                    List<CCDCollectionElement<String>> instructionsClaim = Stream.of("Instruction 1", "Instruction 2")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionClaimant(instructionsClaim);
                    List<CCDCollectionElement<String>> instructionsDef = Stream.of("Instruction 3", "Instruction 4")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionDefendant(instructionsDef);

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).contains(entry("grantExpertReportPermission", YES));
                    assertThat(response.getData()).contains(
                        entry("expertReportInstruction",
                            StringUtils.join(ImmutableList.of("Instruction 1", "Instruction 2", "Instruction 3",
                                "Instruction 4"), ", "))
                    );
                }

                @Test
                void olderCasesShouldNotMigrateData() {
                    ccdCase.setExpertReportPermissionPartyGivenToClaimant(YES);
                    ccdCase.setExpertReportPermissionPartyGivenToDefendant(YES);
                    List<CCDCollectionElement<String>> instructionsClaim = Stream.of("Instruction 1", "Instruction 2")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionClaimant(instructionsClaim);
                    List<CCDCollectionElement<String>> instructionsDef = Stream.of("Instruction 3", "Instruction 4")
                        .map(s -> CCDCollectionElement.<String>builder().value(s).build())
                        .collect(Collectors.toList());
                    ccdCase.setExpertReportInstructionDefendant(instructionsDef);

                    CallbackParams callbackParams = CallbackParams.builder()
                        .type(CallbackType.ABOUT_TO_START)
                        .request(callbackRequest)
                        .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                        .build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                        generateOrderCallbackHandler.handle(callbackParams);

                    assertThat(response.getData()).doesNotContainKeys("grantExpertReportPermission",
                        "expertReportInstruction");
                }
            }
        }
    }
}

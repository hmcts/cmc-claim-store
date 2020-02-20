package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

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
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDExpertReport;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackVersion;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.GenerateOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DRAFTED_BY_LEGAL_ADVISOR;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;

@RunWith(MockitoJUnitRunner.class)
public class GenerateOrderCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final LocalDate DEADLINE = LocalDate.parse("2018-11-16");
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
    private OrderPostProcessor orderPostProcessor;

    @Mock
    private PilotCourtService pilotCourtService;

    private CallbackRequest callbackRequest;
    private GenerateOrderCallbackHandler generateOrderCallbackHandler;
    private CCDCase ccdCase;

    @Before
    public void setUp() {
        OrderCreator orderCreator = new OrderCreator(legalOrderGenerationDeadlinesCalculator, caseDetailsConverter,
            docAssemblyService, new GenerateOrderRule(), directionsQuestionnaireService,
            pilotCourtService);

        generateOrderCallbackHandler = new GenerateOrderCallbackHandler(orderCreator, orderPostProcessor,
            caseDetailsConverter, appInsights);

        ReflectionTestUtils.setField(generateOrderCallbackHandler, "templateId", "testTemplateId");
        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
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
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines()).thenReturn(DEADLINE);
        when(directionsQuestionnaireService.getPreferredCourt(eq(claim))).thenReturn(DEFENDANT_PREFERRED_COURT);

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(GENERATE_ORDER.getValue())
            .build();
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfExpertReportsAreProvided() {
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES)
        );
    }


    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfExpertReportsAreProvidedV2() {
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

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .version(CallbackVersion.V_2)
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES),
            entry("grantExpertReportPermission", NO)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfNobodyObjectsCourt() {
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES)
        );
    }
    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfNobodyObjectsCourtV2() {
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

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(CallbackVersion.V_2)
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", null),
            entry("preferredCourtObjectingParty", null),
            entry("preferredCourtObjectingReason", null),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", YES),
            entry("grantExpertReportPermission", NO)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfOtherDirectionHeaderIsNull() {
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

        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase).toBuilder().hearingCourt(null).build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) generateOrderCallbackHandler.handle(callbackParams);

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
        ).doesNotContain(
            entry("hearingCourt", null)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfOtherDirectionHeaderIsNullV2() {
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

        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase).toBuilder().hearingCourt(null).build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .version(CallbackVersion.V_2)
            .build();
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) generateOrderCallbackHandler.handle(callbackParams);

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
            entry("expertReportPermissionPartyAskedByDefendant", NO),
            entry("grantExpertReportPermission", NO)
        ).doesNotContain(
            entry("hearingCourt", null)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfClaimantsObjectsCourt() {
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
    public void shouldPrepopulateFieldsOnAboutToStartEventIfClaimantsObjectsCourtV2() {
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ));
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .version(CallbackVersion.V_2)
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", "Claimant Court"),
            entry("preferredCourtObjectingParty", "Res_CLAIMANT"),
            entry("preferredCourtObjectingReason", "As a claimant I like this court more"),
            entry("expertReportPermissionPartyAskedByClaimant", YES),
            entry("expertReportPermissionPartyAskedByDefendant", NO),
            entry("grantExpertReportPermission", NO)
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
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", "Defendant Court"),
            entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
            entry("preferredCourtObjectingReason", "As a defendant I like this court more"),
            entry("expertReportPermissionPartyAskedByDefendant", NO),
            entry("expertReportPermissionPartyAskedByClaimant", NO)
        );
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEventIfDefendantObjectsCourtV2() {
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
            .version(CallbackVersion.V_2)
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
            entry("preferredDQCourt", DEFENDANT_PREFERRED_COURT),
            entry("newRequestedCourt", "Defendant Court"),
            entry("preferredCourtObjectingParty", "Res_DEFENDANT"),
            entry("preferredCourtObjectingReason", "As a defendant I like this court more"),
            entry("expertReportPermissionPartyAskedByDefendant", NO),
            entry("expertReportPermissionPartyAskedByClaimant", NO),
            entry("grantExpertReportPermission", NO)
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

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfClaimantResponseIsNotPresent() {
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase).toBuilder()
            .otherDirections(null)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .version(CallbackVersion.V_2)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        generateOrderCallbackHandler
            .handle(callbackParams);
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        generateOrderCallbackHandler.handle(callbackParams);
    }

    @Test
    public void shouldRaiseAppInsight() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        generateOrderCallbackHandler.handle(callbackParams);

        verify(appInsights)
            .trackEvent(DRAFTED_BY_LEGAL_ADVISOR, REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference());
    }
}

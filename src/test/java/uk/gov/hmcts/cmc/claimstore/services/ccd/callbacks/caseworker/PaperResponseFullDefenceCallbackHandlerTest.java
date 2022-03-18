package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.launchdarkly.sdk.LDUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.models.courtfinder.factapi.CourtFinderResponse;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType.form;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseOCON9xFormCallbackHandler.OCON9X_SUBTYPE;
import static uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory.getCCDData;

@ExtendWith(MockitoExtension.class)
class PaperResponseFullDefenceCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String OCON9X_REVIEW =
        "Before continuing you must complete the ‘Review OCON9x paper response’ event";
    private static final List<CaseEvent> CASE_EVENTS = List.of(CaseEvent.PAPER_RESPONSE_OCON_9X_FORM);
    private static final String COURT_FINDER_RESPONSE_NEWCASTLE = "court-finder/response/NEWCASTLE_COURT_FINDER_RESPONSE.json";
    private static final String COURT_NEWCASTLE = "Newcastle Civil & Family Courts and Tribunals Centre";
    private static final String POSTCODE = "postcode";
    private static final String EXAMPLE_EMAIL = "abc@def.com";
    private static final String PREFERRED_DQ_COURT = "preferredDQCourt";
    private static final String OCON_ENHANCEMENTS = "ocon-enhancements";

    @Mock
    PilotCourtService pilotCourtService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private Clock clock;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private CourtFinderApi courtFinderApi;

    @InjectMocks
    private PaperResponseFullDefenceCallbackHandler handler;

    private CallbackParams callbackParams;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private CaseEventService caseEventService;

    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<CCDCase> ccdCaseArgumentCaptor;

    @Mock
    private DefenceResponseNotificationService defenceResponseNotificationService;

    @Mock
    private Claim mockClaim;

    @Nested
    class AboutToStartTests {

        @BeforeEach
        void setUp() {
            CallbackRequest request = CallbackRequest.builder()
                .eventId(CaseEvent.PAPER_RESPONSE_FULL_DEFENCE.getValue())
                .caseDetails(CaseDetails.builder().build())
                .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_START)
                .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .request(request)
                .build();
        }

        @Test
        void showWarningMessage() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(getCCDData(POSTCODE, CCDCase.builder(), CCDPartyType.COMPANY));

            User mockUser = mock(User.class);

            when(userService.getUser(anyString()))
                .thenReturn(mockUser);

            when(launchDarklyClient.isFeatureEnabled(eq(OCON_ENHANCEMENTS), any(LDUser.class)))
                .thenReturn(true);

            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(actualResponse.getErrors().get(0)).isEqualTo(OCON9X_REVIEW);
        }

        @Test
        void showNoWarningMessage() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(getCCDData(POSTCODE, CCDCase.builder(), CCDPartyType.COMPANY));

            CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(courtFinderResponse);

            User mockUser = mock(User.class);

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(userService.getUser(anyString()))
                .thenReturn(mockUser);

            when(launchDarklyClient.isFeatureEnabled(eq(OCON_ENHANCEMENTS), any(LDUser.class)))
                .thenReturn(true);

            when(caseEventService.findEventsForCase(any(String.class), any(User.class)))
                .thenReturn(CASE_EVENTS);

            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(actualResponse.getErrors());
        }

        @Test
        void shouldAddPreferredCourtIfDQsEnabledAndNoPreferredCourtSet() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(getCCDData(POSTCODE, CCDCase.builder(), CCDPartyType.COMPANY));

            CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(courtFinderResponse);

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
                .thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertEquals(COURT_NEWCASTLE, response.getData().get(PREFERRED_DQ_COURT));
        }

        @Test
        void shouldAddPreferredCourtIfDQsEnabledAndPreferredCourtSetForOrganisation() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(getCCDData(POSTCODE,
                CCDCase.builder(), CCDPartyType.ORGANISATION));

            CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(courtFinderResponse);

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertEquals(COURT_NEWCASTLE, response.getData().get(PREFERRED_DQ_COURT));
        }

        @Test
        void shouldAddPreferredCourtIfDQsEnabledAndPreferredCourtSetForSoleTrader() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder()
                .features(ClaimFeatures.DQ_FLAG.getValue())
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .partyDetail(CCDParty.builder()
                            .primaryAddress(null)
                            .build())
                        .claimantProvidedDetail(CCDParty.builder()
                            .primaryAddress(CCDAddress.builder().postCode(POSTCODE).build())
                            .emailAddress(EXAMPLE_EMAIL)
                            .type(CCDPartyType.SOLE_TRADER)
                            .build())
                        .build())
                    .build()))
                .applicants(
                    com.google.common.collect.ImmutableList.of(
                        CCDCollectionElement.<CCDApplicant>builder()
                            .value(SampleData.getCCDApplicantIndividual())
                            .build()
                    ))
                .build());

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

            when(courtFinderApi.findMoneyClaimCourtByPostcode(anyString()))
                .thenReturn(courtFinderResponse);

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertEquals(COURT_NEWCASTLE, response.getData().get(PREFERRED_DQ_COURT));
        }

        @Test
        void shouldAddPreferredCourtWhenPartyDetailIsNull() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder()
                .features(ClaimFeatures.DQ_FLAG.getValue())
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .partyDetail(null)
                        .claimantProvidedDetail(CCDParty.builder()
                            .primaryAddress(CCDAddress.builder().postCode(POSTCODE).build())
                            .emailAddress(EXAMPLE_EMAIL)
                            .type(CCDPartyType.SOLE_TRADER)
                            .build())
                        .build())
                    .build()))
                .applicants(
                    com.google.common.collect.ImmutableList.of(
                        CCDCollectionElement.<CCDApplicant>builder()
                            .value(SampleData.getCCDApplicantIndividual())
                            .build()
                    ))
                .build());

            CourtFinderResponse courtFinderResponse = DataFactory.createCourtFinderResponseFromJson(COURT_FINDER_RESPONSE_NEWCASTLE);

            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(courtFinderResponse);

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertEquals(COURT_NEWCASTLE, response.getData().get(PREFERRED_DQ_COURT));
        }

        @Test
        void shouldAddPreferredCourtIfDQsEnabledAndNoPreferredCourtNotSet() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(getCCDData(POSTCODE,
                CCDCase.builder()
                    .preferredCourt(PREFERRED_DQ_COURT), CCDPartyType.COMPANY));

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(response.getData().get(PREFERRED_DQ_COURT)).isNull();
        }

        @Test
        void shouldHaveCorrectCaseworkerRole() {
            assertThat(handler.getSupportedRoles()).containsOnly(CASEWORKER);
        }

        @Test
        void shouldNotAddPreferredCourtIfDQsNotEnabled() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(CCDCase.builder().build());

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(response.getData().get(PREFERRED_DQ_COURT));
        }

        @Test
        void shouldNotAddPreferredCourtIfPreferredCourtSet() {
            when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
                .thenReturn(Map.of(PREFERRED_DQ_COURT, COURT_NEWCASTLE));

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder()
                .preferredCourt(COURT_NEWCASTLE)
                .build());

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertEquals(COURT_NEWCASTLE, response.getData().get(PREFERRED_DQ_COURT));
        }
    }

    @Nested
    class AboutToSubmitTests {

        private CCDCase ccdCase;

        @BeforeEach
        void setUp() {
            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().data(Map.of("defenceType", CCDDefenceType.DISPUTE.name())).build())
                .eventId(CaseEvent.PAPER_RESPONSE_FULL_DEFENCE.getValue())
                .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .request(request)
                .build();

            when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);

            ccdCase = CCDCase.builder()
                .preferredDQCourt("Central London County Court")
                .respondents(List.of(
                        CCDCollectionElement.<CCDRespondent>builder()
                            .value(CCDRespondent.builder()
                                .partyDetail(CCDParty.builder().build())
                                .claimantProvidedDetail(CCDParty.builder().build())
                                .build())
                            .build()
                    )
                )
                .scannedDocuments(List.of(
                        CCDCollectionElement.<CCDScannedDocument>builder()
                            .value(CCDScannedDocument.builder()
                                .type(form)
                                .subtype(OCON9X_SUBTYPE)
                                .deliveryDate(LocalDateTime.now())
                                .build()
                            ).build()
                    )
                )
                .build();

        }

        @Test
        void shouldSetResponseType() {

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue()
                .getRespondents()
                .stream()
                .map(CCDCollectionElement::getValue)
                .map(CCDRespondent::getResponseType)
                .collect(Collectors.toSet())
            ).containsExactly(CCDResponseType.FULL_DEFENCE);
        }

        @Test
        void shouldSetResponseDefenceType() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue()
                .getRespondents()
                .stream()
                .map(CCDCollectionElement::getValue)
                .map(CCDRespondent::getResponseDefenceType)
                .collect(Collectors.toSet())
            ).containsExactly(CCDDefenceType.DISPUTE);
        }

        @Test
        void shouldSetPartyType() {
            CCDPartyType partyType = CCDPartyType.INDIVIDUAL;

            ccdCase = ccdCase.toBuilder()
                .respondents(List.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(CCDRespondent.builder()
                            .partyDetail(CCDParty.builder().build())
                            .claimantProvidedDetail(CCDParty.builder().type(partyType).build())
                            .build())
                        .build()))
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue()
                .getRespondents()
                .stream()
                .map(CCDCollectionElement::getValue)
                .map(CCDRespondent::getPartyDetail)
                .map(CCDParty::getType)
                .collect(Collectors.toSet())
            ).containsExactly(partyType);
        }

        @Test
        void shouldSetNewFilenameOnOcon9xForm() {

            ccdCase = ccdCase.toBuilder()
                .previousServiceCaseReference("reference")
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue()
                .getScannedDocuments().stream()
                .map(CCDCollectionElement::getValue)
                .map(CCDScannedDocument::getFileName)
                .collect(Collectors.toSet())
            ).containsExactly("reference-scanned-OCON9x-full-defence.pdf");
        }

        @Test
        void shouldNotSetNewFilenameOnNonOcon9xForm() {
            String filename = "filename";
            String subtype = "NON_OCON9x";

            ccdCase = ccdCase.toBuilder()
                .scannedDocuments(ImmutableList.<CCDCollectionElement<CCDScannedDocument>>builder()
                    .addAll(ccdCase.getScannedDocuments())
                    .add(CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(
                            CCDScannedDocument.builder()
                                .fileName(filename)
                                .subtype(subtype)
                                .build()
                        )
                        .build())
                    .build())
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue()
                .getScannedDocuments().stream()
                .map(CCDCollectionElement::getValue)
                .filter(d -> d.getSubtype().equals(subtype))
                .map(CCDScannedDocument::getFileName)
                .collect(Collectors.toSet())
            ).containsExactly(filename);
        }

        @Test
        void shouldSetEmailFromDefandantProvidedAddess() {

            ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
                .scannedDocuments(List.of(
                    CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(CCDScannedDocument.builder()
                            .type(form)
                            .subtype(OCON9X_SUBTYPE)
                            .deliveryDate(LocalDateTime.now())
                            .build())
                        .build())
                )
                .respondents(com.google.common.collect.ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                        .partyDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                            .correspondenceAddress(null)
                            .emailAddress("abc@def.com")
                            .build())
                        .build())
                    .build()))
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);
            handler.handle(callbackParams);
            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
            assertEquals("abc@def.com",
                ccdCaseArgumentCaptor.getValue().getRespondents().get(0).getValue().getPartyDetail().getEmailAddress());
        }

        @Test
        void shouldSetEmailFromClaimantProvidedAddress() {

            ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
                .scannedDocuments(List.of(
                    CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(CCDScannedDocument.builder()
                            .type(form)
                            .subtype(OCON9X_SUBTYPE)
                            .deliveryDate(LocalDateTime.now())
                            .build())
                        .build()))
                .respondents(com.google.common.collect.ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                        .claimantProvidedDetail(ccdCase.getRespondents().get(0).getValue()
                            .getClaimantProvidedDetail().toBuilder()
                            .emailAddress("abc@def.com")
                            .build())
                        .partyDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                            .correspondenceAddress(null)
                            .emailAddress("")
                            .build())
                        .build())
                    .build()))
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);
            handler.handle(callbackParams);
            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
            assertEquals("abc@def.com",
                ccdCaseArgumentCaptor.getValue().getRespondents().get(0).getValue().getPartyDetail().getEmailAddress());
        }

        @Test
        void shouldSetEmailFromClaimantProvidedAddressWhenPartIsNull() {

            ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList()).toBuilder()
                .scannedDocuments(List.of(
                    CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(CCDScannedDocument.builder()
                            .type(form)
                            .subtype(OCON9X_SUBTYPE)
                            .deliveryDate(LocalDateTime.now())
                            .build())
                        .build()))
                .respondents(com.google.common.collect.ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                        .claimantProvidedDetail(ccdCase.getRespondents().get(0).getValue()
                            .getClaimantProvidedDetail().toBuilder()
                            .emailAddress(EXAMPLE_EMAIL)
                            .build())
                        .partyDetail(null)
                        .build())
                    .build()))
                .build();

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(this.ccdCase);

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());
            assertEquals(EXAMPLE_EMAIL,
                ccdCaseArgumentCaptor.getValue().getRespondents().get(0).getValue().getPartyDetail().getEmailAddress());
        }

        @Test
        void shouldSetIntentionToProceedDeadline() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);

            when(caseDetailsConverter.calculateIntentionToProceedDeadline(any(LocalDateTime.class)))
                .thenReturn(DATE.toLocalDate());

            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdCaseArgumentCaptor.capture());

            assertThat(ccdCaseArgumentCaptor.getValue().getIntentionToProceedDeadline()).isEqualTo(DATE.toLocalDate());
        }

        @Test
        void shouldProduceDefencePaperResponseEvent() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(this.ccdCase);

            when(caseDetailsConverter.calculateIntentionToProceedDeadline(any(LocalDateTime.class)))
                .thenReturn(DATE.toLocalDate());

            Claim claim = Claim.builder().referenceNumber("ref").build();
            when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

            handler.handle(callbackParams);

            verify(eventProducer).createDefendantPaperResponseEvent(claim, BEARER_TOKEN);
        }
    }

    @Nested
    class SubmittedTests {

        @BeforeEach
        void setUp() {
            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().data(Map.of("defenceType", CCDDefenceType.DISPUTE.name())).build())
                .eventId(CaseEvent.PAPER_RESPONSE_FULL_DEFENCE.getValue())
                .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.SUBMITTED)
                .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .request(request)
                .build();
        }
    }
}

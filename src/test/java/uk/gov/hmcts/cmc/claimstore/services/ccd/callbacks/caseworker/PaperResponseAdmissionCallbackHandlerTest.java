package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import com.launchdarkly.client.LDUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.CaseEventService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.document.domain.Document;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PAPER_RESPONSE_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType.PART_ADMISSION;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@ExtendWith(MockitoExtension.class)
@DisplayName("Paper Response Admission handler")
class PaperResponseAdmissionCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String OCON9X_REVIEW =
        "Before you continue please esure you review the OCON9x review response";
    private static final List<CaseEvent> CASE_EVENTS = Arrays.asList(CaseEvent.PAPER_RESPONSE_OCON_9X_FORM);
    private PaperResponseAdmissionCallbackHandler handler;
    private CallbackParams callbackParams;
    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    @Mock
    private GeneralLetterService generalLetterService;
    private UserDetails userDetails;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private CaseEventService caseEventService;
    @Mock
    private EventProducer eventProducer;

    @BeforeEach
    void setUp() {
        String paperResponseAdmissionTemplateId = "CV-CMC-GOR-ENG-0016.docx";
        handler = new PaperResponseAdmissionCallbackHandler(caseDetailsConverter,
            defendantResponseNotificationService, caseMapper, docAssemblyService, docAssemblyTemplateBodyMapper,
            paperResponseAdmissionTemplateId, userService, documentManagementService, clock, generalLetterService,
            caseEventService, launchDarklyClient, eventProducer);
        CallbackRequest callbackRequest = getCallBackRequest();
        callbackParams = getBuild(callbackRequest, CallbackType.ABOUT_TO_SUBMIT);

        userDetails = SampleUserDetails.builder()
            .withForename("Forename")
            .withSurname("Surname")
            .withRoles("caseworker-cmc")
            .build();
    }

    private CallbackRequest getCallBackRequest() {
        return CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.EMPTY_MAP).build())
            .eventId(CaseEvent.PAPER_RESPONSE_ADMISSION.getValue())
            .build();
    }

    private CallbackParams getBuild(CallbackRequest callbackRequest, CallbackType aboutToSubmit) {
        return CallbackParams.builder()
            .type(aboutToSubmit)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    void shouldNotChangeFileNameForOtherForm() {

        CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder(), "XXXXX");

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        Assertions.assertThrows(IllegalStateException.class,
            () -> handler.handle(callbackParams));

    }

    @Test
    void notifyClaimant() {

        CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder()
            .defendantId("1234"), "OCON9x");

        Claim claim = Claim.builder()
            .referenceNumber("XXXXX")
            .build();
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        handler.handle(callbackParams);

        verify(defendantResponseNotificationService, times(1)).notifyClaimant(
            any(Claim.class),
            any(String.class)
        );
    }

    @Test
    void shouldNotifyDefendantByEmail() {

        CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder()
            .defendantId("1234"), "OCON9x");

        Claim claim = Claim.builder()
            .referenceNumber("XXXXX")
            .build();
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        handler.handle(callbackParams);

        verify(defendantResponseNotificationService, times(1)).notifyDefendant(
            any(Claim.class),
            any(String.class),
            any(String.class)
        );
    }

    @Test
    void shouldGenerateAndUpdateCaseDocument() {
        CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder(), "OCON9x");

        Claim claim = Claim.builder()
            .referenceNumber("XXXXX")
            .build();
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);
        when(docAssemblyTemplateBodyMapper.paperResponseAdmissionLetter(any(CCDCase.class), any(String.class)))
            .thenReturn(DocAssemblyTemplateBody.builder().build());
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(documentManagementService.getDocumentMetaData(anyString(), anyString())).thenReturn(getLinks());
        when(clock.instant()).thenReturn(LocalDate.parse("2020-06-22").atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(generalLetterService.printLetter(anyString(), any(CCDDocument.class), any(Claim.class)))
            .thenReturn(BulkPrintDetails.builder().build());
        ArgumentCaptor<CCDCase> ccdDataArgumentCaptor = ArgumentCaptor.forClass(CCDCase.class);
        handler.handle(callbackParams);
        verify(caseDetailsConverter).convertToMap(ccdDataArgumentCaptor.capture());

        verify(documentManagementService, times(1))
            .getDocumentMetaData(anyString(), anyString());

        verify(docAssemblyTemplateBodyMapper, times(1)).paperResponseAdmissionLetter(any(CCDCase.class),
            any(String.class));

        assertEquals("CMC-defendant-case-handoff.pdf", ccdDataArgumentCaptor.getValue().getCaseDocuments().get(0)
            .getValue().getDocumentLink().getDocumentFileName());
        assertEquals(GENERAL_LETTER,
            ccdDataArgumentCaptor.getValue().getCaseDocuments().get(0).getValue().getDocumentType());
        assertEquals(1, ccdDataArgumentCaptor.getValue().getCaseDocuments().size());
    }

    @Test
    void shouldThrowExceptionWhenGenerateAndUpdateCaseDocumentFails() {

        CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder(), "OCON9x");

        Claim claim = Claim.builder()
            .referenceNumber("XXXXX")
            .build();
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new RuntimeException("exception"));
        when(docAssemblyTemplateBodyMapper.paperResponseAdmissionLetter(any(CCDCase.class), any(String.class)))
            .thenReturn(DocAssemblyTemplateBody.builder().build());
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

        Assertions.assertThrows(RuntimeException.class,
            () -> handler.handle(callbackParams));
    }

    @Test
    void shouldHaveCorrectCaseworkerRole() {
        assertThat(handler.getSupportedRoles()).containsOnly(CASEWORKER);
    }

    @Test
    void shouldHaveCorrectCaseworkerEvent() {
        assertThat(handler.handledEvents()).containsOnly(PAPER_RESPONSE_ADMISSION);
    }

    @NotNull
    private Document getLinks() {
        Document document = new Document();
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.binary.href = DOC_URL_BINARY;
        document.links = links;
        return document;
    }

    private CCDCase getCCDCase(CCDResponseType fullAdmission, CCDRespondent.CCDRespondentBuilder builder,
                               String ocon9x) {
        return CCDCase.builder()
            .previousServiceCaseReference("CMC")
            .paperAdmissionType(fullAdmission)
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(builder
                    .claimantProvidedDetail(
                        CCDParty.builder()
                            .type(INDIVIDUAL)
                            .build())
                    .partyDetail(CCDParty.builder()
                        .type(INDIVIDUAL)
                        .emailAddress("claimant@email.test")
                        .build())
                    .build())
                .build()))
            .scannedDocuments(List.of(CCDCollectionElement.<CCDScannedDocument>builder()
                .value(CCDScannedDocument.builder()
                    .subtype(ocon9x)
                    .deliveryDate(LocalDateTime.of(2020, Month.JUNE, 17, 11, 30, 57))
                    .build())
                .build()))
            .build();
    }

    @Nested
    class CheckFileName {

        @BeforeEach
        void setUp() {

            Claim claim = Claim.builder()
                .referenceNumber("XXXXX")
                .build();
            when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

            when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
            when(docAssemblyService
                .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
                .thenReturn(docAssemblyResponse);
            when(docAssemblyTemplateBodyMapper.paperResponseAdmissionLetter(any(CCDCase.class), any(String.class)))
                .thenReturn(DocAssemblyTemplateBody.builder().build());
            when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
            when(documentManagementService.getDocumentMetaData(anyString(), anyString())).thenReturn(getLinks());
            when(clock.instant()).thenReturn(LocalDate.parse("2020-06-22").atStartOfDay().toInstant(ZoneOffset.UTC));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
            when(generalLetterService.printLetter(anyString(), any(CCDDocument.class), any(Claim.class)))
                .thenReturn(BulkPrintDetails.builder().build());
        }

        @Test
        void shouldChangeFileNameForOCON9xFormForFullAdmission() {

            CCDCase ccdCase = getCCDCase(FULL_ADMISSION, CCDRespondent.builder(), "OCON9x");

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);

            ArgumentCaptor<CCDCase> ccdDataArgumentCaptor = ArgumentCaptor.forClass(CCDCase.class);
            handler.handle(callbackParams);

            verify(caseDetailsConverter).convertToMap(ccdDataArgumentCaptor.capture());
            assertEquals("CMC-scanned-OCON9x-full-admission.pdf",
                ccdDataArgumentCaptor.getValue().getScannedDocuments().get(0).getValue().getFileName());
            assertNull(ccdDataArgumentCaptor.getValue().getPaperAdmissionType());
            assertEquals(LocalDateTime.of(2020, Month.JUNE, 17, 11, 30, 57),
                ccdDataArgumentCaptor.getValue().getRespondents().get(0).getValue().getResponseSubmittedOn());
            assertEquals(1, ccdDataArgumentCaptor.getValue().getScannedDocuments().size());
        }

        @Test
        void shouldChangeFileNameForOCON9xFormForPartAdmission() {
            CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder(), "OCON9x");

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            ArgumentCaptor<CCDCase> ccdDataArgumentCaptor = ArgumentCaptor.forClass(CCDCase.class);

            handler.handle(callbackParams);
            verify(caseDetailsConverter).convertToMap(ccdDataArgumentCaptor.capture());

            assertEquals("CMC-scanned-OCON9x-part-admission.pdf",
                ccdDataArgumentCaptor.getValue().getScannedDocuments().get(0).getValue().getFileName());
            assertNull(ccdDataArgumentCaptor.getValue().getPaperAdmissionType());
            assertEquals(LocalDateTime.of(2020, Month.JUNE, 17, 11, 30, 57),
                ccdDataArgumentCaptor.getValue().getRespondents().get(0).getValue().getResponseSubmittedOn());
            assertEquals(1, ccdDataArgumentCaptor.getValue().getScannedDocuments().size());
        }

        @Test
        void shouldPrintLetter() {
            CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder(), "OCON9x");
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            handler.handle(callbackParams);
            verify(generalLetterService).printLetter(anyString(), any(CCDDocument.class), any(Claim.class));
        }
    }

    @Nested
    class AboutToStartTests {

        @BeforeEach
        void setUp() {

            CallbackRequest callbackRequest = getCallBackRequest();
            callbackParams = getBuild(callbackRequest, CallbackType.ABOUT_TO_START);
            CCDCase ccdCase = getCCDCase(PART_ADMISSION, CCDRespondent.builder()
                .defendantId("1234"), "OCON9x");
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            User mockUser = mock(User.class);
            when(userService.getUser(anyString())).thenReturn(mockUser);
            when(launchDarklyClient.isFeatureEnabled(eq("ocon-enhancements"), any(LDUser.class))).thenReturn(true);
        }

        @Test
        void showWarningMessage() {
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(actualResponse.getErrors().get(0)).isEqualTo(OCON9X_REVIEW);

        }

        @Test
        void showNoWarningMessage() {

            when(caseEventService.findEventsForCase(any(String.class), any(User.class))).thenReturn(CASE_EVENTS);
            AboutToStartOrSubmitCallbackResponse actualResponse =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(actualResponse.getErrors());

        }
    }
}

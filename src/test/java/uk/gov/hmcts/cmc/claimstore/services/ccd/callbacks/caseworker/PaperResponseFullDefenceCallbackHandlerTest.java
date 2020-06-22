package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaperResponseFullDefenceCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private Clock clock;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private PaperResponseFullDefenceCallbackHandler handler;

    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");

    @Nested
    class AboutToSubmitTests {

        private CallbackParams callbackParams;

        private  CCDCase ccdCase;

        @Captor
        private ArgumentCaptor<CCDCase> ccdCaseArgumentCaptor;

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
                .respondents(List.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(CCDRespondent.builder()
                            .partyDetail(CCDParty.builder().build())
                            .claimantProvidedDetail(CCDParty.builder().build())
                            .build())
                        .build())
                )
                .scannedDocuments(Collections.emptyList())
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
            String filename = "filename";
            String subtype = "OCON9x";

            ccdCase = ccdCase.toBuilder()
                .scannedDocuments(List.of(
                    CCDCollectionElement.<CCDScannedDocument>builder().value(
                        CCDScannedDocument.builder()
                            .fileName(filename)
                            .subtype(subtype)
                            .build()
                    )
                    .build()
                ))
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
                .scannedDocuments(List.of(
                    CCDCollectionElement.<CCDScannedDocument>builder().value(
                        CCDScannedDocument.builder()
                            .fileName(filename)
                            .subtype(subtype)
                            .build()
                    )
                        .build()
                ))
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
            ).containsExactly(filename);
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
}

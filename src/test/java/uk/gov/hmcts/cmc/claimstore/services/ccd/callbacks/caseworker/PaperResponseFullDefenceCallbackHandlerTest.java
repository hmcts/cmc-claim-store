package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

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
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType.form;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseOCON9xFormCallbackHandler.OCON9X_SUBTYPE;

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

    @Mock
    private CourtFinderApi courtFinderApi;

    @InjectMocks
    private PaperResponseFullDefenceCallbackHandler handler;

    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");

    private CallbackParams callbackParams;

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
        void shouldAddPreferredCourtIfDQsEnabledAndNoPreferredCourtSet() {
            String postcode = "postcode";
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder()
                .features(ClaimFeatures.DQ_FLAG.getValue())
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                    .value(CCDRespondent.builder()
                        .partyDetail(CCDParty.builder()
                            .primaryAddress(CCDAddress.builder().postCode(postcode).build())
                            .build())
                        .build())
                    .build()))
                .build());

            String court = "Court";
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(postcode)))
                .thenReturn(List.of(Court.builder().name(court).build()));

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder()
                .features(List.of(ClaimFeatures.DQ_FLAG.getValue()))
                .build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertEquals(court, response.getData().get("preferredDQCourt"));
        }

        @Test
        void shouldNotAddPreferredCourtIfDQsNotEnabled() {
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                .thenReturn(CCDCase.builder().build());

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(Collections.emptyMap());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNull(response.getData().get("preferredDQCourt"));
        }

        @Test
        void shouldNotAddPreferredCourtIfPreferredCourtSet() {
            String court = "Court";
            when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
                .thenReturn(Map.of("preferredDQCourt", court));

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder()
                .preferredCourt(court)
                .build());

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(Claim.builder().build());

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertEquals(court, response.getData().get("preferredDQCourt"));
        }
    }

    @Nested
    class AboutToSubmitTests {

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

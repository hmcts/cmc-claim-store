package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_SIGNED_BY_BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_COUNTER_SIGNED_BY_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class SettlementAgreementMadeCallbackHandlerTest {

    private SettlementAgreementMadeCallbackHandler handler;

    private CallbackParams params;

    private CCDCase ccdCase;

    @Nested
    @DisplayName("State Change for Agreement Counter Signed By Defendant")
    class AgreementCounterSignedByDefendant {
        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT.getValue())
                .build();

            params = CallbackParams.builder()
                .request(callbackRequest)
                .type(CallbackType.ABOUT_TO_SUBMIT).build();

            ccdCase = CCDCase.builder()
                .build();
        }

        @Test
        void shouldReturnOpenStateIfCtscEnabled() {

            handler = new SettlementAgreementMadeCallbackHandler(true);

            String state = ClaimState.SETTLEMENT_AGREEMENT_MADE.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(state, response.getData().get("state"));
        }

        @Test
        void shouldReturnSettlementAgreementMadeStateIfCtscDisabled() {

            handler = new SettlementAgreementMadeCallbackHandler(false);

            String state = ClaimState.OPEN.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            Assertions.assertEquals(state, response.getData().get("state"));
        }
    }

    @Nested
    @DisplayName("State Change for Offer Countersigned By Defendant")
    class OfferCountersignedByDefendant {
        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(OFFER_COUNTER_SIGNED_BY_DEFENDANT.getValue())
                .build();

            params = CallbackParams.builder()
                .request(callbackRequest)
                .type(CallbackType.ABOUT_TO_SUBMIT).build();

            ccdCase = CCDCase.builder()
                .build();
        }

        @Test
        void shouldReturnOpenStateIfCtscEnabled() {

            handler = new SettlementAgreementMadeCallbackHandler(true);

            String state = ClaimState.SETTLEMENT_AGREEMENT_MADE.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(state, response.getData().get("state"));
        }

        @Test
        void shouldReturnSettlementAgreementMadeStateIfCtscDisabled() {

            handler = new SettlementAgreementMadeCallbackHandler(false);

            String state = ClaimState.OPEN.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            Assertions.assertEquals(state, response.getData().get("state"));
        }
    }

    @Nested
    @DisplayName("State Change for Agreement Signed By Both")
    class AgreementSignedByBoth {
        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(AGREEMENT_SIGNED_BY_BOTH.getValue())
                .build();

            params = CallbackParams.builder()
                .request(callbackRequest)
                .type(CallbackType.ABOUT_TO_SUBMIT).build();

            ccdCase = CCDCase.builder()
                .build();
        }

        @Test
        void shouldReturnOpenStateIfCtscEnabled() {

            handler = new SettlementAgreementMadeCallbackHandler(true);

            String state = ClaimState.SETTLEMENT_AGREEMENT_MADE.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(state, response.getData().get("state"));
        }

        @Test
        void shouldReturnSettlementAgreementMadeStateIfCtscDisabled() {

            handler = new SettlementAgreementMadeCallbackHandler(false);

            String state = ClaimState.OPEN.getValue();
            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            Assertions.assertEquals(state, response.getData().get("state"));
        }
    }
}

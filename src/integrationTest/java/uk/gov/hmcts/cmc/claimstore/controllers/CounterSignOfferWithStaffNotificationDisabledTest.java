package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false",
        "feature_toggles.emailToStaff=false"
    }
)
public class CounterSignOfferWithStaffNotificationDisabledTest extends BaseSaveTest {
    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Test
    public void shouldNotSendStaffNotificationWhenCounterSignRequestSubmitted() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        Response response = SampleResponse.validDefaults();

        caseRepository.saveDefendantResponse(claim, "defendant@mmail.com", response, BEARER_TOKEN);

        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.validDefaults(), MadeBy.DEFENDANT);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, "OFFER_MADE_BY_DEFENDANT");

        settlement.accept(MadeBy.CLAIMANT);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, "OFFER_ACCEPTED_BY_CLAIMANT");

        makeRequest(claim.getExternalId());

        verify(emailService, never()).sendEmail(eq("sender@example.com"), emailDataArgument.capture());
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(post("/claims/" + externalId + "/offers/CLAIMANT/countersign")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
            );
    }
}

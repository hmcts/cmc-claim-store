package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PrePaymentTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimReferenceNumber() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        when(userService.getUser(BEARER_TOKEN))
            .thenReturn(new User(BEARER_TOKEN, SampleUserDetails.builder().build()));

        MvcResult result = makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, CaseReference.class))
            .isEqualTo(new CaseReference(claim.getExternalId()));
    }

    private ResultActions makeRequest(String caseReference) throws Exception {
        return webClient
            .perform(
                post("/claims/" + caseReference + "/pre-payment")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            );
    }
}

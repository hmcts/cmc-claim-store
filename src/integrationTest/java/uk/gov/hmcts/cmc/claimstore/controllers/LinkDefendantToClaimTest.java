package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class LinkDefendantToClaimTest extends BaseIntegrationTest {

    @Before
    public void init() {
        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(SampleUserDetails.getDefault());
    }

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        when(coreCaseDataApi.searchForCaseworker(any(), any(), any(), any(), any(), any()))
            .thenReturn(singletonList(
                CaseDetails.builder()
                    .id(123456789L)
                    .build()
                )
            );

        MvcResult result = linkDefendantRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), "1");
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        String nonExistingExternalId = "7d293143-b787-454f-aa8e-2fd69a209e52";

        linkDefendantRequest(nonExistingExternalId)
            .andExpect(status().isNotFound());
    }

    private ResultActions linkDefendantRequest(String externalId) throws Exception {
        return webClient
            .perform(put("/claims/" + externalId + "/defendant/1")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }
}

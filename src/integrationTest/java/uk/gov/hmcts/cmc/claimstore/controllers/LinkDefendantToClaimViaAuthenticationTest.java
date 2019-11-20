package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false",
        "feature_toggles.ccd_async_enabled=false",
        "feature_toggles.ccd_enabled=false",
    }
)
public class LinkDefendantToClaimViaAuthenticationTest extends BaseIntegrationTest {
    static final String username = "defendant.email@htcms.net";
    static final String password = "password";
    static final String urlFormat = "/testing-support/claims/%s/defendant/";

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {

        given(userService.authenticateUser(username, password)).willReturn(SampleUser.builder()
            .withAuthorisation(BEARER_TOKEN)
            .withUserDetails(SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .withMail(DEFENDANT_EMAIL)
                .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
                .build())
            .build()
        );

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        String urlTemplate = String.format(urlFormat, claim.getReferenceNumber());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("defendantUsername", username);
        params.add("defendantPassword", password);

        MockHttpServletRequestBuilder putRequest = put(urlTemplate).params(params)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8");

        webClient.perform(putRequest)
            .andExpect(status().isOk());

        assertThat(claimStore.getClaim(claim.getId()))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), DEFENDANT_ID);
    }

    @Test
    public void shouldReturn401HttpStatusWhenUserDetailsNotExist() throws Exception {

        given(userService.authenticateUser(username, password))
            .willThrow(
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                    "Username & password combination not found",
                    null,
                    null,
                    null)
            );

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        String urlTemplate = String.format(urlFormat, claim.getReferenceNumber());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("defendantUsername", username);
        params.add("defendantPassword", password);

        MockHttpServletRequestBuilder putRequest = put(urlTemplate).params(params)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8");

        webClient
            .perform(putRequest)
            .andExpect(status().isUnauthorized());

        assertThat(claimStore.getClaim(claim.getId()))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), null);

    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimNotExist() throws Exception {

        given(userService.authenticateUser(username, password))
            .willThrow(
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED,
                    "Username & password combination not found",
                    null,
                    null,
                    null)
            );

        String claimReference = "Non Existent Claim Reference";
        String urlTemplate = String.format(urlFormat, claimReference);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("defendantUsername", username);
        params.add("defendantPassword", password);

        MockHttpServletRequestBuilder putRequest = put(urlTemplate).params(params)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8");

        webClient
            .perform(putRequest)
            .andExpect(status().isNotFound());

    }
}

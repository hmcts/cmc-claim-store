package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefendantLinkStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class IsDefendantLinkedTest extends BaseIntegrationTest {

    @Before
    public void setup() {
        UserDetails userDetails = SampleUserDetails.getDefault();
        User user = new User(BEARER_TOKEN, userDetails);
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
        given(userService.authenticateAnonymousCaseWorker()).willReturn(user);
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinked() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        when(userService.getUser(BEARER_TOKEN)).thenReturn(new User(BEARER_TOKEN,
            SampleUserDetails.builder().withRoles("letter-" + claim.getLetterHolderId()).build()));

        caseRepository.linkDefendant(BEARER_TOKEN);

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(true));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenClaimFoundAndIsNotLinked() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenNotClaimFound() throws Exception {
        String nonExistingReferenceNumber = "000MC900";

        MvcResult result = makeRequest(nonExistingReferenceNumber)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }

    private ResultActions makeRequest(String referenceNumber) throws Exception {
        return webClient
            .perform(get("/claims/" + referenceNumber + "/defendant-link-status"));
    }
}

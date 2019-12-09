package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetClaimsByDefendantIdTest extends BaseGetTest {
    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        String defendantId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        when(userService.getUser(BEARER_TOKEN)).thenReturn(new User(BEARER_TOKEN,
            SampleUserDetails.builder().withRoles("letter-" + claim.getLetterHolderId()).build()));

        caseRepository.linkDefendant(BEARER_TOKEN);

        MvcResult result = makeRequest("/claims/defendant/" + defendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getDefendantId).isEqualTo(defendantId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        String nonExistingDefendantId = "900";

        when(userService.getUserDetails(AUTHORISATION_TOKEN))
            .thenReturn(SampleUserDetails.builder().withUserId(nonExistingDefendantId).build());

        MvcResult result = makeRequest("/claims/defendant/" + nonExistingDefendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }
}

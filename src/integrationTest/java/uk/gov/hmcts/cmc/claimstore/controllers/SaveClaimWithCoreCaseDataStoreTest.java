package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.MockedCoreCaseDataApiTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "feature_toggles.async_event_operations_enabled=false"
    }
)
public class SaveClaimWithCoreCaseDataStoreTest extends MockedCoreCaseDataApiTest {

    @Test
    public void shouldStoreRepresentedClaimIntoCCD() throws Exception {

        final Long expectedCaseId = representativeSampleCaseDetails.getId();

        MvcResult result = makeSuccessfulIssueClaimRequestForRepresentative();
        assertThat(result.getResponse().getStatus()).isEqualTo(HTTP_OK);

        final Claim savedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(savedClaim.getId()).isEqualTo(expectedCaseId);
    }

    @Test
    public void shouldStoreCitizenClaimIntoCCD() throws Exception {

        final Long expectedCaseId = citizenSampleCaseDetails.getId();

        MvcResult result = makeSuccessfulIssueClaimRequestForCitizen();
        assertThat(result.getResponse().getStatus()).isEqualTo(HTTP_OK);

        final Claim savedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(savedClaim.getId()).isEqualTo(expectedCaseId);
    }

    @Test
    public void shouldFailIssuingClaimEvenWhenCCDStoreFailsToStartEvent() throws Exception {
        final ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        final String externalId = claimData.getExternalId().toString();

        stubForSearchForRepresentative(externalId);
        stubForStartForRepresentativeWithServerError();

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CASE");
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFailsToSubmitEvent() throws Exception {
        final ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        final String externalId = claimData.getExternalId().toString();

        stubForSearchForRepresentative(externalId);
        stubForStartForRepresentative();
        stubForSubmitForRepresentativeWithServerError(externalId);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CASE");
    }
}

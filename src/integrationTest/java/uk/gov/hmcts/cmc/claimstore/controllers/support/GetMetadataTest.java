package uk.gov.hmcts.cmc.claimstore.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseGetTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.CaseMetadata;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetMetadataTest extends BaseGetTest {

    @Before
    public void init() {
        UserDetails userDetails = SampleUserDetails.getDefault();
        User user = new User(BEARER_TOKEN, userDetails);
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
        given(userService.authenticateAnonymousCaseWorker()).willReturn(user);
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataListWhenClaimsExistForClaimant() throws Exception {
        String submitterId = "1";

        claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = makeRequest("/claims/claimant/" + submitterId + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .first()
            .extracting(CaseMetadata::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimMetadataListWhenClaimsDoNotExistForClaimant() throws Exception {
        String nonExistingSubmitterId = "900";

        MvcResult result = makeRequest("/claims/claimant/" + nonExistingSubmitterId + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataListWhenClaimsExistForDefendant() throws Exception {
        String submitterId = "1";

        ClaimData build = SampleClaimData.builder().build();
        Claim claim = claimStore.saveClaim(build, submitterId, LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(userDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);

        MvcResult result = makeRequest("/claims/defendant/" + DEFENDANT_ID + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .hasSize(1).first()
            .extracting(CaseMetadata::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistForReferenceNumber() throws Exception {
        String submitterId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = makeRequest("/claims/" + claim.getReferenceNumber() + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, CaseMetadata.class))
            .extracting(CaseMetadata::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExistForReferenceNumber() throws Exception {
        makeRequest("/claims/999MC999/metadata")
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistForExternalId() throws Exception {
        String submitterId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = makeRequest("/claims/" + claim.getExternalId() + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, CaseMetadata.class))
            .extracting(CaseMetadata::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn404HttpStatusWhenExternalIdDoesNotExist() throws Exception {
        makeRequest("/claims/" + UUID.randomUUID().toString() + "/metadata")
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404HttpStatusWhenReferenceNumberOrExternalIdFormatIsInvalid() throws Exception {
        makeRequest("/claims/abcdefgh/metadata")
            .andExpect(status().isNotFound());
    }

    private List<CaseMetadata> deserializeMetadataListFrom(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<CaseMetadata>>() {
        });
    }
}

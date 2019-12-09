package uk.gov.hmcts.cmc.claimstore.deprecated.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.metadata.CaseMetadata;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetMetadataTest extends BaseGetTest {

    private static final String ANONYMOUS_CASEWORKER_ID = "100";
    private static final String CASEWORKER_ROLE = "caseworker-cmc";

    @Before
    public void init() {
        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(ANONYMOUS_CASEWORKER_ID)
            .withRoles(CASEWORKER_ROLE).build();
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
            .extracting(CaseMetadata::getSubmitterId).isEqualTo(submitterId);
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
            .extracting(CaseMetadata::getSubmitterId).isEqualTo(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistForReferenceNumber() throws Exception {
        String submitterId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = makeRequest("/claims/" + claim.getReferenceNumber() + "/metadata")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, CaseMetadata.class))
            .extracting(CaseMetadata::getSubmitterId).isEqualTo(submitterId);
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
            .extracting(CaseMetadata::getSubmitterId).isEqualTo(submitterId);
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

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistWithSubmitterEmail() throws Exception {
        String submitterId = "1";
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = webClient.perform(post("/claims/filters/claimants/email")
            .param("email", claim.getSubmitterEmail())
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .extracting(CaseMetadata::getSubmitterId).containsOnly(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistWithDefendantEmail() throws Exception {
        claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());

        MvcResult result = webClient.perform(post("/claims/filters/defendants/email")
            .param("email", SampleTheirDetails.DEFENDANT_EMAIL)
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .extracting(CaseMetadata::getDefendantId).containsOnly(DEFENDANT_ID);
    }

    @Test
    public void shouldReturn200HttpStatusAndNoDataWhenNoClaimsExistWithSubmitterEmail() throws Exception {
        MvcResult result = webClient.perform(post("/claims/filters/claimants/email")
            .param("email", "unknown@nowhere.net")
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn200HttpStatusAndNoDataWhenNoClaimsExistWithDefendantEmail() throws Exception {
        MvcResult result = webClient.perform(post("/claims/filters/defendants/email")
            .param("email", "unknown@nowhere.net")
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeMetadataListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn200HttpStatusAndClaimMetadataWhenClaimsExistForCreatedState() throws Exception {
        claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());

        MvcResult result = makeRequest("/claims/filters/created")
            .andExpect(status().isOk())
            .andReturn();

        List<CaseMetadata> actual = deserializeMetadataListFrom(result);
        assertThat(actual).isNotEmpty();
        assertThat(actual).extracting(CaseMetadata::getState).containsOnly(CREATE);
    }

    private List<CaseMetadata> deserializeMetadataListFrom(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<CaseMetadata>>() {
        });
    }
}

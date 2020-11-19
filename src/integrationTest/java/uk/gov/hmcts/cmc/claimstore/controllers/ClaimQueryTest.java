package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefendantLinkStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimQueryTest extends BaseMockSpringTest {
    private static final String ROOT_PATH = "/claims";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CITIZEN.getRole())
        .withUserId(SampleClaim.USER_ID).build();
    private static final User USER = new User(AUTHORISATION_TOKEN, USER_DETAILS);

    private static final User CASEWORKER = new User(AUTHORISATION_TOKEN, SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole()).build());

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected CaseRepository caseRepository;

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(USER);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void testGetBySubmitterId() throws Exception {
        Claim claim = SampleClaim.getDefault();
        when(caseRepository.getBySubmitterId(SampleClaim.USER_ID, AUTHORISATION_TOKEN, ""))
            .thenReturn(List.of(claim));

        List<Claim> retrievedClaims = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/claimant/{submitterId}", SampleClaim.USER_ID)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(retrievedClaims)
            .hasSize(1)
            .first().extracting(Claim::getExternalId)
            .isEqualTo(claim.getExternalId());
    }

    @Test
    public void testGetBySubmitterIdRejectedByOtherUser() throws Exception {
        doGet(ROOT_PATH + "/claimant/{submitterId}", SampleClaim.USER_ID + "diff")
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetByLetterHolderID() throws Exception {
        Claim claim = SampleClaim.getDefault();
        when(caseRepository.getByLetterHolderId(SampleClaim.LETTER_HOLDER_ID, AUTHORISATION_TOKEN))
            .thenReturn(Optional.of(claim));

        Claim retrievedClaim = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/letter/{letterHolderId}", SampleClaim.LETTER_HOLDER_ID)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(retrievedClaim).isNotNull();
    }

    @Test
    public void testGetByLetterHolderIdNotFound() throws Exception {
        when(caseRepository.getByLetterHolderId(anyString(), anyString()))
            .thenReturn(Optional.empty());

        doGet(ROOT_PATH + "/letter/{letterHolderId}", SampleClaim.LETTER_HOLDER_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetByLetterHolderIdRejectedByOtherUser() throws Exception {
        String submitterId = SampleClaim.USER_ID + "diff";
        Claim claim = SampleClaim.getDefault().toBuilder()
            .submitterId(submitterId)
            .build();
        when(caseRepository.getByLetterHolderId(SampleClaim.LETTER_HOLDER_ID, AUTHORISATION_TOKEN))
            .thenReturn(Optional.of(claim));

        doGet(ROOT_PATH + "/letter/{letterHolderId}", SampleClaim.LETTER_HOLDER_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetByExternalId() throws Exception {
        Claim claim = SampleClaim.getDefault();
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        Claim retrievedClaim = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/{externalId}", SampleClaim.EXTERNAL_ID)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(retrievedClaim).isNotNull();
    }

    @Test
    public void testGetByExternalIdNotFound() throws Exception {
        when(caseRepository.getClaimByExternalId(anyString(), any(User.class)))
            .thenReturn(Optional.empty());
        doGet(ROOT_PATH + "/{externalId}", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetByExternalIdRejectedByOtherUser() throws Exception {
        String submitterId = SampleClaim.USER_ID + "diff";
        Claim claim = SampleClaim.getDefault().toBuilder()
            .submitterId(submitterId)
            .build();
        when(caseRepository.getClaimByExternalId(SampleClaim.EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        doGet(ROOT_PATH + "/{externalId}", SampleClaim.EXTERNAL_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetByClaimReference() throws Exception {
        Claim claim = SampleClaim.getDefault();
        when(caseRepository.getByClaimReferenceNumber(SampleClaim.REFERENCE_NUMBER, AUTHORISATION_TOKEN))
            .thenReturn(Optional.of(claim));
        Claim retrievedClaim = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/{claimReference}", SampleClaim.REFERENCE_NUMBER)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(retrievedClaim).isNotNull();
    }

    @Test
    public void testGetByClaimReferenceNotFound() throws Exception {
        when(caseRepository.getByClaimReferenceNumber(anyString(), anyString()))
            .thenReturn(Optional.empty());
        doGet(ROOT_PATH + "/{claimReference}", SampleClaim.REFERENCE_NUMBER)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetByClaimReferenceRejectedByOtherUser() throws Exception {
        String submitterId = SampleClaim.USER_ID + "diff";
        Claim claim = SampleClaim.getDefault().toBuilder()
            .submitterId(submitterId)
            .build();
        when(caseRepository.getByClaimReferenceNumber(SampleClaim.REFERENCE_NUMBER, AUTHORISATION_TOKEN))
            .thenReturn(Optional.of(claim));

        doGet(ROOT_PATH + "/{claimReference}", SampleClaim.REFERENCE_NUMBER)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetByExternalClaimReference() throws Exception {
        Claim claim = SampleClaim.getDefaultForLegal();
        Claim otherClaim = SampleClaim.getDefaultForLegal().toBuilder()
            .referenceNumber("999LR999")
            .externalId(UUID.randomUUID().toString())
            .claimData(claim.getClaimData().toBuilder()
                .externalReferenceNumber(SampleClaimData.EXTERNAL_REFERENCE_NUMBER + "diff")
                .build())
            .build();

        when(caseRepository.getBySubmitterId(SampleClaim.USER_ID, AUTHORISATION_TOKEN, ""))
            .thenReturn(List.of(otherClaim, claim));

        List<Claim> retrievedClaims = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/representative/{externalReference}", SampleClaimData.EXTERNAL_REFERENCE_NUMBER)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(retrievedClaims)
            .hasSize(1)
            .first()
            .extracting(legalClaim -> legalClaim.getClaimData().getExternalReferenceNumber())
            .matches(Optional::isPresent)
            .extracting(Optional::orElseThrow)
            .isEqualTo(SampleClaimData.EXTERNAL_REFERENCE_NUMBER);
    }

    @Test
    public void testGetByExternalClaimReferenceNotFound() throws Exception {
        Claim otherClaim = SampleClaim.getDefaultForLegal();
        otherClaim = otherClaim.toBuilder()
            .referenceNumber("999LR999")
            .externalId(UUID.randomUUID().toString())
            .claimData(otherClaim.getClaimData().toBuilder()
                .externalReferenceNumber(SampleClaimData.EXTERNAL_REFERENCE_NUMBER + "diff")
                .build())
            .build();

        when(caseRepository.getBySubmitterId(SampleClaim.USER_ID, AUTHORISATION_TOKEN, ""))
            .thenReturn(List.of(otherClaim));

        List<Claim> retrievedClaims = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/representative/{externalReference}", SampleClaimData.EXTERNAL_REFERENCE_NUMBER)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertThat(retrievedClaims)
            .isEmpty();
    }

    @Test
    public void testGetByDefendantId() throws Exception {
        String defendantId = SampleClaim.USER_ID;
        Claim claim = SampleClaim.getDefault().toBuilder()
            // swap user roles
            .submitterId(SampleClaim.USER_ID + "claimant")
            .defendantId(defendantId)
            .build();
        when(caseRepository.getByDefendantId(defendantId, AUTHORISATION_TOKEN, ""))
            .thenReturn(List.of(claim));

        List<Claim> retrievedClaims = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/defendant/{defendantId}", defendantId)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(retrievedClaims)
            .hasSize(1)
            .first().isNotNull();
    }

    @Test
    public void testGetByDefendantIdRejectedByOtherUser() throws Exception {
        doGet(ROOT_PATH + "/defendant/{defendantId}", SampleClaim.LETTER_HOLDER_ID)
            .andExpect(status().isForbidden());
    }

    @Test
    public void testIsDefendantLinkedTrue() throws Exception {
        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(CASEWORKER);

        when(caseRepository.getByClaimReferenceNumber(eq(SampleClaim.REFERENCE_NUMBER), any()))
            .thenReturn(Optional.of(SampleClaim.getDefault()));
        DefendantLinkStatus response = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/{caseReference}/defendant-link-status", SampleClaim.REFERENCE_NUMBER)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(response)
            .isNotNull()
            .matches(DefendantLinkStatus::isLinked);
    }

    @Test
    public void testIsDefendantLinkedFalse() throws Exception {
        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(CASEWORKER);

        when(caseRepository.getByClaimReferenceNumber(eq(SampleClaim.REFERENCE_NUMBER), any()))
            .thenReturn(Optional.of(SampleClaim.getDefaultWithoutResponse(SampleClaim.DEFENDANT_EMAIL)));
        DefendantLinkStatus response = jsonMappingHelper.fromJson(
            doGet(ROOT_PATH + "/{caseReference}/defendant-link-status", SampleClaim.REFERENCE_NUMBER)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertThat(response)
            .isNotNull()
            .matches(not(DefendantLinkStatus::isLinked));
    }
}

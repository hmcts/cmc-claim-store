package uk.gov.hmcts.cmc.claimstore.deprecated.controllers.errors;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
@ActiveProfiles("mocked-database-tests")
public class EndpointErrorsTest extends MockSpringTest {

    private static final Exception UNEXPECTED_ERROR
        = new UnableToExecuteStatementException("Unexpected error", (StatementContext) null);
    private static final String BEARER_TOKEN = "Bearer token";

    private static final String CLAIMANT_ID = "1";

    private static final User USER = new User(BEARER_TOKEN, SampleUserDetails.builder()
        .withUserId(CLAIMANT_ID)
        .withMail("claimant@email.com")
        .build());

    @Autowired
    private MockMvc webClient;

    @Test
    public void searchByExternalIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String externalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        given(caseRepository.getClaimByExternalId(externalId, USER)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void searchBySubmitterIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String submitterId = "1";

        given(caseRepository.getBySubmitterId(submitterId, BEARER_TOKEN)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/claimant/" + submitterId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void searchByDefendantIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String defendantId = "1";

        given(claimRepository.getByDefendantId(defendantId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/defendant/" + defendantId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void linkDefendantToClaimShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String defendantId = "2";

        given(claimRepository.getByDefendantId(defendantId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/defendant/" + defendantId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void linkDefendantToClaimShouldReturn500HttpStatusWhenFailedToUpdateClaim() throws Exception {
        String externalId = "2ab19d16-fddf-4494-a01a-f64f93d04782";
        String defendantId = "2";

        Claim claim = SampleClaim.builder()
            .withExternalId(externalId)
            .withDefendantId(null)
            .build();
        given(claimRepository.getClaimByExternalId(externalId)).willReturn(Optional.of(claim));
        given(claimRepository.linkDefendant(claim.getLetterHolderId(), defendantId, DEFENDANT_EMAIL))
            .willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(put("/claims/defendant/link")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void retrieveDefendantLinkStatusShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String referenceNumber = "000MC001";

        given(claimRepository.getByClaimReferenceNumber(referenceNumber)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/" + referenceNumber + "/defendant-link-status"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void requestForMoreTimeShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String externalId = "84f1dda3-e205-4277-96a6-1f23b6f1766d";

        given(caseRepository.getClaimByExternalId(externalId, any())).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(post("/claims/" + externalId + "/request-more-time")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void getByClaimReferenceNumberShouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        String referenceNumber = "000MC001";

        given(testingSupportRepository.getByClaimReferenceNumber(referenceNumber)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/testing-support/claims/" + referenceNumber)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void saveClaimShouldReturnConflictForDuplicateClaimFailures() throws Exception {

        Exception duplicateKeyError = new UnableToExecuteStatementException(new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"external_id_unique\"", null), null);

        given(userService.getUser(anyString()))
            .willReturn(USER);

        given(claimRepository.saveRepresented(anyString(), anyString(), any(LocalDate.class),
            any(LocalDate.class), anyString(), anyString(), anyString(), anyString()))
            .willThrow(duplicateKeyError);

        webClient
            .perform(post("/claims/" + CLAIMANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .header("Features", ImmutableList.of("admissions"))
                .content(jsonMapper.toJson(SampleClaimData.validDefaults()))
            )
            .andExpect(status().isConflict());
    }

    @Test
    public void saveResponseShouldFailWhenDefendantResponseFailedStoring() throws Exception {

        Claim claim = SampleClaim.getDefault();
        String externalId = claim.getExternalId();

        given(caseRepository.getClaimByExternalId(externalId, any()))
            .willReturn(Optional.of(claim));

        willThrow(UNEXPECTED_ERROR).given(claimRepository).saveDefendantResponse(
            anyString(),
            anyString(),
            any(LocalDate.class),
            anyString()
        );

        webClient
            .perform(post("/responses/claim/" + externalId + "/defendant/" + DEFENDANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .content(jsonMapper.toJson(SampleResponse.validDefaults()))
            )
            .andExpect(status().isInternalServerError());
    }
}

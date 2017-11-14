package uk.gov.hmcts.cmc.claimstore.controllers.errors;

import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmccase.models.sampledata.SampleResponseData;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class EndpointErrorsTest extends MockSpringTest {

    private static final Exception UNEXPECTED_ERROR
        = new UnableToExecuteStatementException("Unexpected error", (StatementContext) null);

    @Autowired
    private MockMvc webClient;

    @Test
    public void searchByExternalIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String externalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        given(claimRepository.getClaimByExternalId(externalId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/" + externalId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void searchBySubmitterIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String submitterId = "1";

        given(claimRepository.getBySubmitterId(submitterId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/claimant/" + submitterId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void searchByDefendantIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String defendantId = "1";

        given(claimRepository.getByDefendantId(defendantId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/defendant/" + defendantId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void linkDefendantToClaimShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long claimId = 1L;

        given(claimRepository.getById(claimId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(put("/claims/" + claimId + "/defendant/2"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void linkDefendantToClaimShouldReturn500HttpStatusWhenFailedToUpdateClaim() throws Exception {
        long claimId = 1L;
        String defendantId = "2";

        given(claimRepository.getById(claimId)).willReturn(Optional.of(SampleClaim.builder()
            .withClaimId(claimId)
            .withDefendantId(null)
            .build()));
        given(claimRepository.linkDefendant(claimId, defendantId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(put("/claims/" + claimId + "/defendant/" + defendantId))
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
        long claimId = 1L;

        given(claimRepository.getById(claimId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(post("/claims/" + claimId + "/request-more-time")
                .header(HttpHeaders.AUTHORIZATION, "it's me!"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void getByClaimReferenceNumberShouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        String referenceNumber = "000MC001";

        given(claimRepository.getByClaimReferenceNumber(referenceNumber)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/testing-support/claims/" + referenceNumber))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void saveClaimShouldReturnConflictForDuplicateClaimFailures() throws Exception {
        String claimantId = "1";

        Exception duplicateKeyError = new UnableToExecuteStatementException(new PSQLException(
            "ERROR: duplicate key value violates unique constraint \"external_id_unique\"", null), null);

        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.builder()
            .withUserId(claimantId)
            .withMail("claimant@email.com")
            .build());

        given(claimRepository.saveRepresented(anyString(), anyString(), any(LocalDate.class),
            any(LocalDate.class), anyString(), anyString()))
            .willThrow(duplicateKeyError);

        webClient
            .perform(post("/claims/" + claimantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(SampleClaimData.validDefaults()))
            )
            .andExpect(status().isConflict());
    }

    @Test
    public void saveResponseShouldFailWhenDefendantResponseFailedStoring() throws Exception {
        long claimId = 1L;
        String defendantId = "2";

        given(claimRepository.getById(claimId)).willReturn(Optional.of(SampleClaim.getDefault()));
        willThrow(UNEXPECTED_ERROR).given(claimRepository).saveDefendantResponse(anyLong(), anyString(), anyString(),
            anyString());

        webClient
            .perform(post("/responses/claim/" + claimId + "/defendant/" + defendantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(SampleResponseData.validDefaults()))
            )
            .andExpect(status().isInternalServerError());
    }
}

package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.time.LocalDate;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.EXTERNAL_ID;

public class ResourceExceptionHandlerTest extends BaseTest {
    private static final String CLAIMANT_ID = "123";

    @Before
    public void setup() {
        UnableToExecuteStatementException exception = mock(UnableToExecuteStatementException.class);
        PSQLException psqlException = mock(PSQLException.class);
        when(exception.getCause()).thenReturn(psqlException);
        final String errorMessage = "ERROR: duplicate key value violates unique constraint \"external_id_unique\"";
        when(exception.getCause().getMessage()).thenReturn(errorMessage);

        given(claimRepository.saveRepresented(anyString(), anyString(), any(LocalDate.class),
            any(LocalDate.class), anyString(), anyString()))
            .willThrow(exception);

        given(userService.getUserDetails(anyString())).willReturn(
            SampleUserDetails.builder().withUserId(CLAIMANT_ID).withMail("claimant@email.com").build());

        given(holidaysCollection.getPublicHolidays()).willReturn(emptySet());
    }

    @Test
    public void shouldReturnNotImplementedForPatchOnAnEndpoint() throws Exception {
        webClient
            .perform(patch("/claims/" + EXTERNAL_ID))
            .andExpect(status().isNotImplemented())
            .andReturn();
    }

    @Test
    public void shouldReturnConflictForDuplicateClaimFailures() throws Exception {
        postClaim(SampleClaimData.validDefaults())
            .andExpect(status().isConflict())
            .andReturn();
    }

    private ResultActions postClaim(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + CLAIMANT_ID)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claimData))
            );
    }
}

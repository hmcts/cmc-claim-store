package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetResponseByDefendantIdTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusAndResponseListWhenResponsesExist() throws Exception {
        long defendantId = 1L;

        given(defendantResponseRepository.getByDefendantId(defendantId))
            .willReturn(Lists.newArrayList(newResponse(1L, defendantId)));

        MvcResult result = webClient
            .perform(get("/responses/defendant/" + defendantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).containsExactly(newResponse(1L, defendantId));
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyResponseListWhenResponseDoNotExist() throws Exception {
        long defendantId = 1L;

        given(defendantResponseRepository.getByDefendantId(defendantId))
            .willReturn(Lists.newArrayList());

        MvcResult result = webClient
            .perform(get("/responses/defendant/" + defendantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).isEmpty();
    }

    @Test
    public void shouldReturn404HttpStatusWhenDefendantParameterIsNotNumber() throws Exception {
        webClient
            .perform(get("/responses/defendant/not-a-number"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveResponse() throws Exception {
        long defendantId = 1L;

        given(defendantResponseRepository.getByDefendantId(defendantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/responses//defendant/" + defendantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    private DefendantResponse newResponse(Long id, Long defendantId) {
        return new DefendantResponse(id, 3L, defendantId, null, null, null);
    }

    private List<DefendantResponse> deserialize(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(),
            new TypeReference<List<DefendantResponse>>() {});
    }

}

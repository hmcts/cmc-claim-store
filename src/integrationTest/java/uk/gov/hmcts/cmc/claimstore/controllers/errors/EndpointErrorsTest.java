package uk.gov.hmcts.cmc.claimstore.controllers.errors;

import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EndpointErrorsTest extends BaseMockSpringTest {

    @Autowired
    protected CaseRepository caseRepository;

    @MockBean
    protected EmailService emailService;

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
    public void requestForMoreTimeShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String externalId = "84f1dda3-e205-4277-96a6-1f23b6f1766d";

        given(caseRepository.getClaimByExternalId(externalId, USER)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(post("/claims/" + externalId + "/request-more-time")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isInternalServerError());
    }
}

package uk.gov.hmcts.cmc.claimstore.controllers.errors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: revert mock configuration to run tests in this class
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
public class EndpointErrorsTest {

    @Autowired
    protected MockMvc webClient;

    @MockBean
    protected ClaimRepository claimRepository;

    @MockBean
    protected PublicHolidaysCollection holidaysCollection;

    @MockBean
    protected NotificationClient notificationClient;

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected PDFServiceClient pdfServiceClient;

    @MockBean
    protected UserService userService;

    @Test
    public void getByExternalId_shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        given(claimRepository.getClaimByExternalId("efa77f92-6fb6-45d6-8620-8662176786f1"))
                .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
                .perform(get("/claims/efa77f92-6fb6-45d6-8620-8662176786f1"))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

    @Test
    public void getBySubmitterId_shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long claimantId = 1L;

        given(claimRepository.getBySubmitterId(claimantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void getByDefendantId_shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long defendantId = 1L;

        given(claimRepository.getByDefendantId(defendantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/defendant/" + defendantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }


}

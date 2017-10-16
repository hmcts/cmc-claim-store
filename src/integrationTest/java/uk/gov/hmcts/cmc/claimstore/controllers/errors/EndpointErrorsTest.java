package uk.gov.hmcts.cmc.claimstore.controllers.errors;

import org.flywaydb.core.Flyway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
@ActiveProfiles("test")
public class EndpointErrorsTest {

    private static final Exception UNEXPECTED_ERROR
            = new UnableToExecuteStatementException("Unexpected error", (StatementContext) null);

    @TestConfiguration
    static class MockedConfiguration {

        @MockBean
        private Flyway flyway;

        @MockBean
        private PublicHolidaysCollection holidaysCollection;

        @MockBean
        private NotificationClient notificationClient;

        @MockBean
        private EmailService emailService;

        @MockBean
        private PDFServiceClient pdfServiceClient;

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new PlatformTransactionManager() {

                @Override
                public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                    return null;
                }

                @Override
                public void commit(TransactionStatus status) throws TransactionException {

                }

                @Override
                public void rollback(TransactionStatus status) throws TransactionException {

                }
            };
        }

    }

    @Autowired
    private MockMvc webClient;

    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private UserService userService;

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
        long submitterId = 1L;

        given(claimRepository.getBySubmitterId(submitterId)).willThrow(UNEXPECTED_ERROR);

        webClient
            .perform(get("/claims/claimant/" + submitterId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void searchByDefendantIdShouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long defendantId = 1L;

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
        long defendantId = 2L;

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
        long claimantId = 1L;

        Exception duplicateKeyError = new UnableToExecuteStatementException(new PSQLException(
                "ERROR: duplicate key value violates unique constraint \"external_id_unique\"", null), null);

        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.builder()
            .withUserId(claimantId)
            .withMail("claimant@email.com")
            .build());

        given(claimRepository.saveRepresented(anyString(), anyLong(), any(LocalDate.class),
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
}

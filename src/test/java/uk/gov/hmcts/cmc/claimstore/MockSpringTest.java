package uk.gov.hmcts.cmc.claimstore;

import org.flywaydb.core.Flyway;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
@ActiveProfiles("unit-tests")
public abstract class MockSpringTest {

    @TestConfiguration
    @Profile("unit-tests")
    static class MockedDatabaseConfiguration {

        @MockBean
        private Flyway flyway;

        @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
        private DataSource dataSource;

        @MockBean
        private ClaimRepository claimRepository;

        @Bean
        protected PlatformTransactionManager transactionManager() {
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
    protected JsonMapper jsonMapper;

    @Autowired
    protected ClaimRepository claimRepository;

    @MockBean
    protected UserService userService;

    @MockBean
    protected PublicHolidaysCollection holidaysCollection;

    @MockBean
    protected NotificationClient notificationClient;

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected PDFServiceClient pdfServiceClient;

}

package uk.gov.hmcts.cmc.claimstore;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.flywaydb.core.Flyway;
import org.quartz.Scheduler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import javax.sql.DataSource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {
        ClaimStoreApplication.class,
        TestIdamConfiguration.class
    }
)
@TestPropertySource("/environment.properties")
class ApplicationStartupTest {

    @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
    private DataSource dataSource;
    @MockBean(name = "transactionAwareDataSourceProxy")
    private TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;
    @MockBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;
    @MockBean
    private TestingSupportRepository testingSupportRepository;
    @MockBean
    private Flyway flyway;
    @MockBean
    private JobService jobService;
    @MockBean
    private SpringBeanJobFactory springBeanJobFactory;
    @MockBean
    private SchedulerFactoryBean schedulerFactoryBean;
    @MockBean
    private Scheduler scheduler;
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextStarts() {
        // test passes if Spring context starts successfully
    }
}

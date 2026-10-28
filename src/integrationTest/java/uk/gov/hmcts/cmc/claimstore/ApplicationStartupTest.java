package uk.gov.hmcts.cmc.claimstore;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean(name = "dataSource", answers = Answers.RETURNS_MOCKS)
    private DataSource dataSource;
    @MockitoBean(name = "transactionAwareDataSourceProxy")
    private TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;
    @MockitoBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;
    @MockitoBean
    private TestingSupportRepository testingSupportRepository;
    @MockitoBean
    private Flyway flyway;
    @MockitoBean
    private JobService jobService;
    @MockitoBean
    private SpringBeanJobFactory springBeanJobFactory;
    // Overrides the Scheduler that QuartzConfiguration's SchedulerFactoryBean creates, so Quartz never starts.
    // Spring Framework 7 can't override a FactoryBean itself, only the object it produces.
    @MockitoBean(name = "schedulerFactoryBean")
    private Scheduler schedulerFactoryBeanScheduler;
    @MockitoBean
    private Scheduler scheduler;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextStarts() {
        // test passes if Spring context starts successfully
    }
}

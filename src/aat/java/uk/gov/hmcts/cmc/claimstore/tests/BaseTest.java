package uk.gov.hmcts.cmc.claimstore.tests;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.CommonOperations;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;
import uk.gov.hmcts.cmc.email.EmailService;

import javax.sql.DataSource;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/environment.properties", properties = "feature_toggles.create_claim_enabled = true")
@ActiveProfiles({
    "aat",
    "mocked-database-tests"
})
public abstract class BaseTest {

    @Autowired
    protected Bootstrap bootstrap;

    @Autowired
    protected JsonMapper jsonMapper;

    @MockitoBean
    protected EmailService emailService;

    @Autowired
    protected IdamTestService idamTestService;

    @Autowired
    protected CommonOperations commonOperations;

    @Autowired
    protected TestData testData;

    // The AAT tests run against a deployed instance, so the local context gets no database or scheduler.
    // @MockitoBean is only honoured on test classes, which is why these live here rather than in a
    // @Configuration class.
    @MockitoBean
    private Flyway flyway;

    @MockitoBean
    private ClaimRepository claimRepository;

    @MockitoBean
    private TestingSupportRepository testingSupportRepository;

    @MockitoBean(name = "dataSource", answers = Answers.RETURNS_MOCKS)
    private DataSource dataSource;

    @MockitoBean
    private SpringBeanJobFactory springBeanJobFactory;

    @MockitoBean(name = "schedulerFactoryBean")
    private Scheduler schedulerFactoryBeanScheduler;

    @MockitoBean(name = "scheduler")
    private Scheduler scheduler;

    @MockitoBean(name = "transactionAwareDataSourceProxy")
    private TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;

    @MockitoBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;
}

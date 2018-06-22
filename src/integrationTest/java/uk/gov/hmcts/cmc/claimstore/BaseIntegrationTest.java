package uk.gov.hmcts.cmc.claimstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@DirtiesContext
@TestExecutionListeners(listeners = {BaseIntegrationTest.CleanDatabaseListener.class}, mergeMode = MERGE_WITH_DEFAULTS)
public abstract class BaseIntegrationTest extends MockSpringTest {
    protected static final String SUBMITTER_ID = "123";
    protected static final String DEFENDANT_ID = "555";
    protected static final String DEFENDANT_EMAIL = "j.smith@example.com";
    protected static final String BEARER_TOKEN = "Bearer let me in";
    protected static final String SERVICE_TOKEN = "S2S token";

    @Autowired
    protected ClaimStore claimStore;

    public static class CleanDatabaseListener extends AbstractTestExecutionListener {
        @Override
        public void beforeTestClass(TestContext testContext) {
            ApplicationContext applicationContext = testContext.getApplicationContext();
            DataSource dataSource = applicationContext.getBean("claimStoreDataSource", DataSource.class);
            JdbcTestUtils.deleteFromTables(new JdbcTemplate(dataSource), "claim");
        }
    }
}

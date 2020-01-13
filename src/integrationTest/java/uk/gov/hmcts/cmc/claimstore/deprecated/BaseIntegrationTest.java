package uk.gov.hmcts.cmc.claimstore.deprecated;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import javax.sql.DataSource;

import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@DirtiesContext
@TestExecutionListeners(listeners = {BaseIntegrationTest.CleanDatabaseListener.class}, mergeMode = MERGE_WITH_DEFAULTS)
public abstract class BaseIntegrationTest extends MockSpringTest {
    protected static final String SUBMITTER_ID = "123";
    protected static final String DEFENDANT_ID = "555";
    protected static final String DEFENDANT_EMAIL = "j.smith@example.com";
    protected static final String BEARER_TOKEN = "Bearer let me in";
    protected static final String SERVICE_TOKEN = "S2S token";

    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    protected static final String SOLICITOR_AUTHORISATION_TOKEN = "Solicitor Bearer token";

    protected static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    protected static final String USER_ID = "1";
    protected static final String JURISDICTION_ID = "CMC";
    protected static final String CASE_TYPE_ID = "MoneyClaimCase";
    protected static final boolean IGNORE_WARNING = true;

    @Autowired
    protected ClaimStore claimStore;

    @Autowired
    protected PostClaimOperation postClaimOperation;

    public static class CleanDatabaseListener extends AbstractTestExecutionListener {
        @Override
        public void beforeTestClass(TestContext testContext) {
            ApplicationContext applicationContext = testContext.getApplicationContext();
            DataSource dataSource = applicationContext.getBean("claimStoreDataSource", DataSource.class);
            JdbcTestUtils.deleteFromTables(new JdbcTemplate(dataSource), "claim");
        }
    }

    protected ResultActions makeIssueClaimRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("Features", ImmutableList.of("admissions"))
                .content(jsonMapper.toJson(claimData))
            );
    }

    protected ImmutableMap<String, String> searchCriteria(String externalId) {
        return ImmutableMap.of(
            "page", "1",
            "sortDirection", "desc",
            "case.externalId", externalId
        );
    }

    protected ResultActions makeGetRequest(String urlTemplate) throws Exception {
        return webClient.perform(
            get(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
        );
    }
}

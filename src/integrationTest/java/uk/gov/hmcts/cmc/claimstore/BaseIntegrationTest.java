package uk.gov.hmcts.cmc.claimstore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@TestExecutionListeners(listeners = {BaseIntegrationTest.CleanDatabaseListener.class}, mergeMode = MERGE_WITH_DEFAULTS)
public abstract class BaseIntegrationTest extends MockSpringTest {

    protected static final String SUBMITTER_ID = "123";
    protected static final String DEFENDANT_ID = "555";
    protected static final String DEFENDANT_EMAIL = "j.smith@example.com";
    protected static final String BEARER_TOKEN = "Bearer let me in";

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected ClaimStore claimStore;

    @Autowired
    protected CaseMapper caseMapper;

    public static class CleanDatabaseListener extends AbstractTestExecutionListener {

        @Override
        public void beforeTestClass(TestContext testContext) throws Exception {
            ApplicationContext applicationContext = testContext.getApplicationContext();
            DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
            JdbcTestUtils.deleteFromTables(new JdbcTemplate(dataSource), "claim");
        }
    }

    protected List<String> extractErrors(MvcResult result) throws UnsupportedEncodingException {
        return Arrays.stream(result.getResponse()
            .getContentAsString()
            .split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    protected <T> T deserializeObjectFrom(MvcResult result, Class<T> targetClass) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), targetClass);
    }

    protected List<Claim> deserializeListFrom(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }
}

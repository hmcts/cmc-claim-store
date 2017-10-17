package uk.gov.hmcts.cmc.claimstore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ContextConfiguration(initializers = BaseIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ActiveProfiles(profiles = "integration-tests", inheritProfiles = false)
public abstract class BaseIntegrationTest extends MockSpringTest {

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment(configurableApplicationContext.getEnvironment(),
                "CLAIM_STORE_DB_HOST=" + postgres.getContainerIpAddress(),
                "CLAIM_STORE_DB_PORT=" + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                "CLAIM_STORE_DB_USERNAME=" + postgres.getUsername(),
                "CLAIM_STORE_DB_PASSWORD=" + postgres.getPassword()
            );
        }
    }

    protected static final Long SUBMITTER_ID = 123L;
    protected static final Long DEFENDANT_ID = 555L;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:9.6")
        .withDatabaseName("claimstore");

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected ClaimStore claimStore;

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

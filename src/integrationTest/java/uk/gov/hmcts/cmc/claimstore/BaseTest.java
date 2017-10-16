package uk.gov.hmcts.cmc.claimstore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.DefendantResponseRepository;
import uk.gov.hmcts.cmc.claimstore.services.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BaseTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource("/environment.properties")
public abstract class BaseTest {

    protected static final Long CLAIM_ID = 1L;
    protected static final Long SUBMITTER_ID = 123L;
    protected static final Long LETTER_HOLDER_ID = 999L;
    protected static final Long DEFENDANT_ID = 555L;
    protected static final String REFERENCE_NUMBER = "000MC001";
    protected final Claim claimAfterSavingWithResponse = SampleClaim.getWithDefaultResponse();

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:9.6")
        .withDatabaseName("claimstore");

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

    @Autowired
    protected ClaimRepository claimRepository;

    @Autowired
    protected ClaimStore claimStore;

    @Autowired
    protected DefendantResponseRepository defendantResponseRepository;

    @Autowired
    protected JsonMapper jsonMapper;

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

    @Autowired
    protected MockMvc webClient;

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

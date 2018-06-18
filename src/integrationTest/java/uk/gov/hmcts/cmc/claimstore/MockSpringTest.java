package uk.gov.hmcts.cmc.claimstore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.scheduler.services.JobService;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
@DirtiesContext
@ContextConfiguration(initializers = {MockSpringTest.Initializer.class})
public abstract class MockSpringTest {

    @ClassRule
    public static PostgreSQLContainer claimStorePostgreSQLContainer =
        (PostgreSQLContainer) new PostgreSQLContainer("postgres:10.4")
            .withDatabaseName("claimstore")
            .withUsername("claimstore")
            .withPassword("claimstore")
            .withStartupTimeout(Duration.ofSeconds(600));

    @ClassRule
    public static PostgreSQLContainer schedulerPostgreSQLContainer =
        (PostgreSQLContainer) new PostgreSQLContainer("postgres:10.4")
            .withDatabaseName("scheduler")
            .withUsername("scheduler")
            .withPassword("scheduler")
            .withStartupTimeout(Duration.ofSeconds(600));

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected ClaimRepository claimRepository;

    @Autowired
    protected CaseRepository caseRepository;

    @Autowired
    protected TestingSupportRepository testingSupportRepository;

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

    @MockBean
    protected AuthTokenGenerator authTokenGenerator;

    @MockBean
    protected DocumentMetadataDownloadClientApi documentMetadataDownloadClient;

    @MockBean
    protected DocumentDownloadClientApi documentDownloadClient;

    @MockBean
    protected DocumentUploadClientApi documentUploadClient;

    @MockBean
    protected CoreCaseDataApi coreCaseDataApi;

    @MockBean
    protected CaseAccessApi caseAccessApi;

    @MockBean
    protected ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    protected JobService jobService;

    @MockBean
    protected AppInsights appInsights;

    @Autowired
    protected MockMvc webClient;

    @Autowired
    protected CaseMapper caseMapper;

    protected <T> T deserializeObjectFrom(MvcResult result, Class<T> targetClass) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), targetClass);
    }

    protected List<Claim> deserializeListFrom(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }

    static class Initializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.claimstore.url=" + claimStorePostgreSQLContainer.getJdbcUrl(),
                "spring.datasource.claimstore.username=" + claimStorePostgreSQLContainer.getUsername(),
                "spring.datasource.claimstore.password=" + claimStorePostgreSQLContainer.getPassword(),
                "spring.datasource.scheduler.url=" + schedulerPostgreSQLContainer.getJdbcUrl(),
                "spring.datasource.scheduler.username=" + schedulerPostgreSQLContainer.getUsername(),
                "spring.datasource.scheduler.password=" + schedulerPostgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}

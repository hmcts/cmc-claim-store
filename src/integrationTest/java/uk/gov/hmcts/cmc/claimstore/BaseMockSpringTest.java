package uk.gov.hmcts.cmc.claimstore;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import org.flywaydb.core.Flyway;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.helper.JsonMappingHelper;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.scheduler.services.JobService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.docassembly.DocAssemblyApi;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.service.notify.NotificationClient;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
public abstract class BaseMockSpringTest {

    protected static final String SUBMITTER_ID = "123";
    protected static final String BEARER_TOKEN = "Bearer let me in";
    protected static final String SERVICE_TOKEN = "S2S token";
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    protected static final String USER_ID = "1";
    protected static final String JURISDICTION_ID = "CMC";
    protected static final String CASE_TYPE_ID = "MoneyClaimCase";
    protected static final boolean IGNORE_WARNING = true;
    
    protected static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withMail("submitter@example.com")
        .build();

    @Autowired
    protected CaseMapper caseMapper;
    @Autowired
    protected CaseDetailsConverter caseDetailsConverter;
    @Autowired
    protected JsonMappingHelper jsonMappingHelper;
    @Autowired
    protected MockMvc webClient;

    @MockBean
    protected OrderDrawnNotificationService orderDrawnNotificationService;
    @MockBean
    protected DocumentManagementService documentManagementService;
    @MockBean
    protected LegalOrderService legalOrderService;
    @MockBean
    protected UserService userService;
    @MockBean
    protected CourtFinderApi courtFinderApi;
    @MockBean
    protected DocAssemblyApi docAssemblyApi;
    @MockBean
    protected AuthTokenGenerator authTokenGenerator;
    @MockBean
    protected AppInsights appInsights;
    @MockBean
    protected ReferenceNumberRepository referenceNumberRepository;
    @MockBean
    protected CoreCaseDataApi coreCaseDataApi;
    @MockBean
    protected PaymentsService paymentsService;
    @MockBean
    protected EventProducer eventProducer;
    @MockBean
    protected PDFServiceClient pdfServiceClient;
    @MockBean
    protected TelemetryClient telemetry;
    @MockBean
    protected SendLetterApi sendLetterApi;
    @MockBean
    protected NotificationClient notificationClient;
    @MockBean
    protected JobService jobService;
    @MockBean
    protected PilotCourtService pilotCourtService;
    @MockBean
    protected DirectionOrderService directionOrderService;

    @MockBean
    private Flyway flyway;
    @MockBean
    private TestingSupportRepository testingSupportRepository;
    @MockBean(name = "dataSource", answer = Answers.RETURNS_MOCKS)
    private DataSource dataSource;
    @MockBean
    private SpringBeanJobFactory springBeanJobFactory;
    @MockBean
    private SchedulerFactoryBean schedulerFactoryBean;
    @MockBean
    private Scheduler scheduler;
    @MockBean(name = "transactionAwareDataSourceProxy")
    private TransactionAwareDataSourceProxy transactionAwareDataSourceProxy;
    @MockBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

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

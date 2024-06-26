package uk.gov.hmcts.cmc.claimstore;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.helper.JsonMappingHelper;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.BankHolidays;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.scheduler.services.JobService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.docassembly.DocAssemblyApi;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.service.notify.NotificationClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        ClaimStoreApplication.class,
        TestIdamConfiguration.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
public abstract class BaseMockSpringTest {

    protected static final String SUBMITTER_ID = "123";
    protected static final String BEARER_TOKEN = "Bearer letmein";

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
    protected SecuredDocumentManagementService securedDocumentManagementService;
    @MockBean
    protected LegalOrderService legalOrderService;
    @MockBean
    protected UserService userService;
    @MockBean(name = "courtFinderApi")
    protected CourtFinderApi courtFinderApi;
    @MockBean(name = "docAssemblyApi")
    protected DocAssemblyApi docAssemblyApi;
    @MockBean
    protected AuthTokenGenerator authTokenGenerator;
    @MockBean
    protected AppInsights appInsights;
    @MockBean
    protected ReferenceNumberRepository referenceNumberRepository;
    @MockBean(name = "coreCaseDataApi")
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
    protected BankHolidaysApi bankHolidaysApi;
    @MockBean
    protected Authentication authentication;
    @MockBean
    protected SecurityContext securityContext;
    @MockBean
    protected JwtDecoder jwtDecoder;
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

    @BeforeEach
    public void setUpBase() {

        bankHolidaysSetup();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        setSecurityAuthorities(authentication);
        when(jwtDecoder.decode(anyString())).thenReturn(getJwt());
    }

    private void bankHolidaysSetup() {
        String input = new ResourceReader().read("/bank-holidays.json");
        BankHolidays bankHolidays = jsonMappingHelper.fromJson(input, BankHolidays.class);
        given(bankHolidaysApi.retrieveAll()).willReturn(bankHolidays);
    }

    protected void setSecurityAuthorities(Authentication authenticationMock, String... authorities) {

        Jwt jwt = getJwt();
        when(authenticationMock.getPrincipal()).thenReturn(jwt);

        Collection<? extends GrantedAuthority> authorityCollection = Stream.of(authorities)
            .map(a -> new SimpleGrantedAuthority(a))
            .collect(Collectors.toCollection(ArrayList::new));

        when(authenticationMock.getAuthorities()).thenAnswer(invocationOnMock -> authorityCollection);

    }

    @NotNull
    private Jwt getJwt() {
        return Jwt.withTokenValue(BEARER_TOKEN)
            .claim("exp", Instant.ofEpochSecond(1585763216))
            .claim("iat", Instant.ofEpochSecond(1585734416))
            .claim("token_type", "Bearer")
            .claim("tokenName", "access_token")
            .claim("expires_in", 28800)
            .header("kid", "b/O6OvVv1+y+WgrH5Ui9WTioLt0=")
            .header("typ", "RS256")
            .header("alg", "RS256")
            .build();
    }

    protected ImmutableMap<String, String> searchCriteria(String externalId) {
        return ImmutableMap.of(
            "page", "1",
            "sortDirection", "desc",
            "case.externalId", externalId
        );
    }

    protected ResultActions doGet(String urlTemplate, Object... uriVars) throws Exception {
        return webClient.perform(
            get(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }

    protected <T> ResultActions doPost(String auth, T content, String urlTemplate, Object... uriVars) throws Exception {
        return webClient.perform(
            post(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMappingHelper.toJson(content)));
    }

    protected <T> ResultActions doPut(String auth, T content, String urlTemplate, Object... uriVars) throws Exception {
        return webClient.perform(
            put(urlTemplate, uriVars)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .header("LetterHolderID", SampleClaim.LETTER_HOLDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMappingHelper.toJson(content)));
    }

}

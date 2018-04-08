package uk.gov.hmcts.cmc.claimstore;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.TestingSupportRepository;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.service.notify.NotificationClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
public abstract class MockSpringTest {

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
    protected CoreCaseDataService coreCaseDataService;

    @MockBean
    protected AppInsights appInsights;
}

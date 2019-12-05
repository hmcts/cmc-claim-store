package uk.gov.hmcts.cmc.claimstore;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.helper.JsonMappingHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyApi;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/environment.properties")
public abstract class BaseMockSpringTest {

    @Autowired
    protected CaseDetailsConverter caseDetailsConverter;
    @Autowired
    protected JsonMappingHelper jsonMappingHelper;
    @Autowired
    protected MockMvc webClient;

    @MockBean
    protected EmailService emailService;
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
}

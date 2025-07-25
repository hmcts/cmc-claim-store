package uk.gov.hmcts.cmc.claimstore.tests;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.CommonOperations;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.TestData;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;
import uk.gov.hmcts.cmc.email.EmailService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/environment.properties", properties = "feature_toggles.create_claim_enabled = true")
@ActiveProfiles({
    "aat",
    "mocked-database-tests"
})
public abstract class BaseTest {

    @Autowired
    protected Bootstrap bootstrap;

    @Autowired
    protected JsonMapper jsonMapper;

    @MockBean
    protected EmailService emailService;

    @Autowired
    protected IdamTestService idamTestService;

    @Autowired
    protected CommonOperations commonOperations;

    @Autowired
    protected TestData testData;
}

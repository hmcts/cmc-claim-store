package uk.gov.hmcts.cmc.claimstore.tests;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("/environment.properties")
@ActiveProfiles({
    "aat",
    "mocked-database-tests"
})
public abstract class BaseTest {

    @Autowired
    protected Bootstrap bootstrap;

    @Autowired
    protected JsonMapper jsonMapper;

}

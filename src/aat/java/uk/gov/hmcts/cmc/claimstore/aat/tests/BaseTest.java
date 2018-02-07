package uk.gov.hmcts.cmc.claimstore.aat.tests;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("/environment.properties")
@ActiveProfiles({
    "aat",
    "mocked-database-tests"
})
public abstract class BaseTest {

}

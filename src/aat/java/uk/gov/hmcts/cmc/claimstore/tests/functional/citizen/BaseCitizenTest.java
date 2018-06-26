package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

@TestPropertySource({
    "/environment-citizen.properties",
    "/environment.properties"
})
public abstract class BaseCitizenTest extends BaseTest {
}

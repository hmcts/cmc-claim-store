package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

@TestPropertySource({"/environment-solicitor.properties", "/environment.properties"})
public abstract class BaseSolicitorTest extends BaseTest {
}

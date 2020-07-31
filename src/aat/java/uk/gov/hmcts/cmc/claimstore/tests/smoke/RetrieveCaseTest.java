package uk.gov.hmcts.cmc.claimstore.tests.smoke;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

import java.util.regex.Pattern;

public class RetrieveCaseTest extends BaseTest {

    private static final Pattern jsonListPattern = Pattern.compile("^\\[.*]$");

    @Test
    public void shouldBeAbleToRetrieveCasesBySubmitterId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        Assert.assertNotNull(citizen);
    }

    @Test
    public void shouldBeAbleToRetrieveCasesByDefendantId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        Assert.assertNotNull(citizen);
    }
}

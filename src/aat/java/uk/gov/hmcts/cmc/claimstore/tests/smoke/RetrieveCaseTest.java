package uk.gov.hmcts.cmc.claimstore.tests.smoke;

    import org.junit.Test;
    import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

    import java.util.regex.Pattern;

public class RetrieveCaseTest extends BaseTest {

    private static final Pattern jsonListPattern = Pattern.compile("^\\[.*\\]$");

    @Test
    public void shouldBeAbleToRetrieveCasesBySubmitterId() {
        commonOperations
            .testCasesRetrievalFor("/claims/claimant/" + bootstrap.getSmokeTestCitizen().getUserDetails().getId());
    }

    @Test
    public void shouldBeAbleToRetrieveCasesByDefendantId() {
        commonOperations
            .testCasesRetrievalFor("/claims/defendant/" + bootstrap.getSmokeTestCitizen().getUserDetails().getId());
    }
}

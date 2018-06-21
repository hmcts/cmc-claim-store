package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;

import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StatementOfMeansContentProviderTest {

    private StatementOfMeansContentProvider provider = new StatementOfMeansContentProvider();

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideStatementOfMeans() {
        StatementOfMeans statementOfMeans = SampleStatementOfMeans.builder().build();

        Map<String, Object> content = provider.createContent(statementOfMeans);

        assertThat(content)
            .containsKeys("selfEmployment", "bankAccounts", "children",
                "residence", "maintainedChildren", "incomes", "otherDependants",
                "courtOrders", "expenses", "debts", "dependant", "employment");
    }
}

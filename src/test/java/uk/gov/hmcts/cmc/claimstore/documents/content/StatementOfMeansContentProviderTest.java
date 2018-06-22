package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
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

    @Test
    public void shouldProvideJobTypeEmployedAndSelfmployed() {
        Employment employment = Employment.builder()
            .employers(asList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
            .selfEmployment(SelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                .build())
            .build();
        String jobType = provider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Employed and self-employed");
    }

    @Test
    public void shouldProvideJobTypeSelfEmployed() {
        Employment employment = Employment.builder()
            .selfEmployment(SelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                .build())
            .build();
        String jobType = provider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Self-employed");
    }

}

package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatementOfMeansContentProviderTest {

    private final StatementOfMeansContentProvider provider = new StatementOfMeansContentProvider();

    @Test
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class, () -> {
            provider.createContent(null);
        });
    }

    @Test
    public void shouldProvideStatementOfMeans() {
        StatementOfMeans statementOfMeans = SampleStatementOfMeans.builder().build();

        Map<String, Object> content = provider.createContent(statementOfMeans);

        assertThat(content)
            .containsKeys("bankAccounts", "children",
                "residence", "maintainedChildren", "incomes", "otherDependants",
                "courtOrders", "expenses", "debts", "dependant", "employment");
    }

    @Test
    public void shouldProvideJobTypeEmployedAndSelfEmployed() {
        StatementOfMeansContentProvider.JobTypeContentProvider jobTypeContentProvider =
            new StatementOfMeansContentProvider.JobTypeContentProvider();

        Employment employment = Employment.builder()
            .employers(singletonList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
            .selfEmployment(SelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                .build())
            .build();
        String jobType = jobTypeContentProvider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Employed and self-employed");
    }

    @Test
    public void shouldProvideJobTypeSelfEmployed() {
        StatementOfMeansContentProvider.JobTypeContentProvider jobTypeContentProvider =
            new StatementOfMeansContentProvider.JobTypeContentProvider();
        Employment employment = Employment.builder()
            .selfEmployment(SelfEmployment.builder()
                .jobTitle("Director")
                .annualTurnover(TEN)
                .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                .build())
            .build();
        String jobType = jobTypeContentProvider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Self-employed");
    }

    @Test
    public void shouldProvideJobTypeEmployed() {
        StatementOfMeansContentProvider.JobTypeContentProvider jobTypeContentProvider =
            new StatementOfMeansContentProvider.JobTypeContentProvider();
        Employment employment = Employment.builder()
            .employers(singletonList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
            .build();
        String jobType = jobTypeContentProvider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Employed");
    }

    @Test
    public void shouldProvideJobTypeRetired() {
        StatementOfMeansContentProvider.JobTypeContentProvider jobTypeContentProvider =
            new StatementOfMeansContentProvider.JobTypeContentProvider();
        Employment employment = Employment.builder()
            .unemployment(Unemployment.builder().retired(true).build())
            .build();
        String jobType = jobTypeContentProvider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Retired");
    }

    @Test
    public void shouldProvideJobTypeUnEmployed() {
        StatementOfMeansContentProvider.JobTypeContentProvider jobTypeContentProvider =
            new StatementOfMeansContentProvider.JobTypeContentProvider();
        Employment employment = Employment.builder()
            .unemployment(Unemployment.builder().retired(false).build())
            .build();
        String jobType = jobTypeContentProvider.createJobType(employment);
        assertThat(jobType)
            .isEqualTo("Unemployed");
    }

}

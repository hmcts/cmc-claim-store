package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;

import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;
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
    public void shouldProvideStatementOfMeansWithJobTypeEmployedAndSelfmployed() {
        StatementOfMeans statementOfMeans = StatementOfMeans.builder()
            .residence(Residence.builder().type(Residence.ResidenceType.JOINT_OWN_HOME).build())
            .employment(Employment.builder()
                .employers(asList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
                .unemployment(Unemployment.builder().retired(true).build())
                .selfEmployment(SelfEmployment.builder()
                    .jobTitle("Director")
                    .annualTurnover(TEN)
                    .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                    .build())
                .build()
            )
            .build();

        Map<String, Object> content = provider.createContent(statementOfMeans);

        assertThat(content)
            .containsKeys("jobType");
        assertThat(content)
            .containsValues("Employed and self-employed");
    }

    @Test
    public void shouldProvideStatementOfMeansWithJobTypeSelfEmployed() {
        StatementOfMeans statementOfMeans = StatementOfMeans.builder()
            .residence(Residence.builder().type(Residence.ResidenceType.JOINT_OWN_HOME).build())
            .employment(Employment.builder()
                .unemployment(Unemployment.builder().retired(true).build())
                .selfEmployment(SelfEmployment.builder()
                    .jobTitle("Director")
                    .annualTurnover(TEN)
                    .onTaxPayments(OnTaxPayments.builder().amountYouOwe(TEN).reason("Genuine reason").build())
                    .build())
                .build()
            )
            .build();

        Map<String, Object> content = provider.createContent(statementOfMeans);

        assertThat(content)
            .containsKeys("jobType");
        assertThat(content)
            .containsValues("Self-employed");
    }

}

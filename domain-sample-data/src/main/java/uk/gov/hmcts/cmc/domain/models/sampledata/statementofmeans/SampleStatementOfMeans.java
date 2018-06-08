package uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.UnEmployment;

import java.math.BigDecimal;

import static java.util.Arrays.asList;

public class SampleStatementOfMeans {

    public static SampleStatementOfMeans builder() {
        return new SampleStatementOfMeans();
    }

    public StatementOfMeans build() {
        return StatementOfMeans.builder()
            .residence(Residence.builder().type(Residence.ResidenceType.JOINT_OWN_HOME).build())
            .reason("My reason")
            .bankAccounts(asList(BankAccount.builder()
                .type(BankAccount.BankAccountType.SAVINGS_ACCOUNT)
                .joint(true)
                .balance(BigDecimal.TEN)
                .build()
            ))
            .courtOrders(asList(CourtOrder.builder()
                .amountOwed(BigDecimal.TEN)
                .claimNumber("Reference")
                .monthlyInstalmentAmount(BigDecimal.ONE)
                .build()
            ))
            .debts(asList(Debt.builder()
                .totalOwed(BigDecimal.TEN)
                .description("Reference")
                .monthlyPayments(BigDecimal.ONE)
                .build()
            ))
            .expenses(asList(Expense.builder()
                .type(Expense.ExpenseType.COUNCIL_TAX)
                .frequency(PaymentFrequency.MONTH)
                .amountPaid(BigDecimal.TEN)
                .build()
            ))
            .incomes(asList(Income.builder()
                .type(Income.IncomeType.JOB)
                .frequency(PaymentFrequency.MONTH)
                .amountReceived(BigDecimal.TEN)
                .build()
            ))
            .dependant(Dependant.builder().build()
            )
            .employment(Employment.builder()
                .employers(asList(Employer.builder().employerName("CMC").jobTitle("My sweet job").build()))
                .unEmployment(UnEmployment.builder().retired(true).build())
                .build()
            )
            .build();
    }

}

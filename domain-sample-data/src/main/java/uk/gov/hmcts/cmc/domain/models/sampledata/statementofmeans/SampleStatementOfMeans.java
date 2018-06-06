package uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans;

import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.ResidenceType;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.math.BigDecimal;

import static java.util.Arrays.asList;

public class SampleStatementOfMeans {

    public static SampleStatementOfMeans builder() {
        return new SampleStatementOfMeans();
    }

    public StatementOfMeans build() {
        return StatementOfMeans.builder()
            .residenceType(ResidenceType.OWN_HOME)
            .reason("My reason")
            .bankAccounts(asList(BankAccount.builder()
                .typeOfAccount(BankAccount.BankAccountType.SAVING_ACCOUNT)
                .jointOption(YesNoOption.YES)
                .balance(BigDecimal.TEN)
                .build()
            ))
            .courtOrders(asList(CourtOrder.builder()
                .amountOwed(BigDecimal.TEN)
                .details("Reference")
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
                .type("Salary")
                .frequency(PaymentFrequency.MONTH)
                .amountPaid(BigDecimal.TEN)
                .build()
            ))
            .incomes(asList(Income.builder()
                .type("Salary")
                .frequency(PaymentFrequency.MONTH)
                .amountReceived(BigDecimal.TEN)
                .build()
            ))
            .dependant(Dependant.builder()
                .children(Children.builder().between11and15(0).between16and19(1).between16and19(2).build())
                .maintainedChildren(1)
                .build()
            )
            .employment(Employment.builder()
                .employmentOption(YesNoOption.YES)
                .employers(asList(Employer.builder().employerName("CMC").jobTitle("My sweet job").build()))
                .selfEmployedOption(YesNoOption.NO)
                .build()
            )
            .build();
    }

}

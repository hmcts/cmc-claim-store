package uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans;

import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.math.BigDecimal;

import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;

public class SampleStatementOfMeans {

    public static SampleStatementOfMeans builder() {
        return new SampleStatementOfMeans();
    }

    public StatementOfMeans build() {
        return StatementOfMeans.builder()
            .residence(Residence.builder().type(Residence.ResidenceType.JOINT_OWN_HOME).build())
            .dependant(Dependant.builder()
                .children(asList(Child.builder()
                    .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
                    .numberOfChildren(2)
                    .build()))
                .numberOfMaintainedChildren(2)
                .otherDependants(OtherDependants.builder()
                    .numberOfPeople(3)
                    .details("Three other dependants")
                    .build())
                .build())
            .employment(Employment.builder()
                .employers(asList(Employer.builder().name("CMC").jobTitle("My sweet job").build()))
                .unemployment(null)
                .selfEmployment(null)
                .build()
            )
            .reason("My reason")
            .bankAccounts(asList(BankAccount.builder()
                .type(BankAccount.BankAccountType.SAVINGS_ACCOUNT)
                .joint(true)
                .balance(TEN)
                .build()
            ))
            .debts(asList(Debt.builder()
                .totalOwed(TEN)
                .description("Reference")
                .monthlyPayments(BigDecimal.ONE)
                .build()
            ))
            .courtOrders(asList(CourtOrder.builder()
                .amountOwed(TEN)
                .claimNumber("Reference")
                .monthlyInstalmentAmount(BigDecimal.ONE)
                .build()
            ))
            .expenses(asList(Expense.builder()
                .type(Expense.ExpenseType.COUNCIL_TAX)
                .frequency(PaymentFrequency.MONTH)
                .amount(TEN)
                .build()
            ))
            .incomes(asList(Income.builder()
                .type(Income.IncomeType.JOB)
                .frequency(PaymentFrequency.MONTH)
                .amount(TEN)
                .build()
            ))
            .partner(LivingPartner.builder()
                .declared(true)
                .ageGroup(LivingPartner.AgeGroupType.ADULT)
                .disability(DisabilityStatus.NO)
                .build())
            .disability(DisabilityStatus.YES)
            .build();
    }

}

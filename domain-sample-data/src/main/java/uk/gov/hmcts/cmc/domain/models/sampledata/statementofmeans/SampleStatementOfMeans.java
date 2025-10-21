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
import java.util.Collections;

public class SampleStatementOfMeans {

    public static final BigDecimal AMOUNT = new BigDecimal("10.99");
    public static final BigDecimal ONE_99 = new BigDecimal("1.99");

    public static SampleStatementOfMeans builder() {
        return new SampleStatementOfMeans();
    }

    public StatementOfMeans build() {
        return StatementOfMeans.builder()
            .residence(Residence.builder().type(Residence.ResidenceType.JOINT_OWN_HOME).build())
            .dependant(Dependant.builder()
                .children(Collections.singletonList(Child.builder()
                    .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
                    .numberOfChildren(2)
                    .id("fa73da34-66c7-4909-8771-478f5bf1ffb7")
                    .build()))
                .numberOfMaintainedChildren(2)
                .otherDependants(OtherDependants.builder()
                    .numberOfPeople(3)
                    .details("Three other dependants")
                    .anyDisabled(false)
                    .build())
                .anyDisabledChildren(false)
                .build())
            .employment(Employment.builder()
                .employers(Collections.singletonList(Employer.builder()
                    .id("0bf39079-eec1-4740-b14c-bd9427f9cc50")
                    .name("CMC")
                    .jobTitle("My sweet job")
                    .build()))
                .unemployment(null)
                .selfEmployment(null)
                .build()
            )
            .reason("My reason")
            .bankAccounts(Collections.singletonList(BankAccount.builder()
                .id("5adfe417-0611-4e54-8751-4fb8ed600bf1")
                .type(BankAccount.BankAccountType.SAVINGS_ACCOUNT)
                .joint(true)
                .balance(AMOUNT)
                .build()
            ))
            .debts(Collections.singletonList(Debt.builder()
                .id("d354b56d-8ec1-49ef-9650-57bca975e283")
                .totalOwed(AMOUNT)
                .description("Reference")
                .monthlyPayments(ONE_99)
                .build()
            ))
            .courtOrders(Collections.singletonList(CourtOrder.builder()
                .id("fffb3d88-6b67-4be3-a835-0df2db49f7f7")
                .amountOwed(AMOUNT)
                .claimNumber("Reference")
                .monthlyInstalmentAmount(ONE_99)
                .build()
            ))
            .expenses(Collections.singletonList(Expense.builder()
                .id("60a0179b-7f14-4a8c-a7cc-170877eadc76")
                .type(Expense.ExpenseType.COUNCIL_TAX)
                .frequency(PaymentFrequency.MONTH)
                .amount(AMOUNT)
                .build()
            ))
            .incomes(Collections.singletonList(Income.builder()
                .id("c4fc229d-87db-43ff-8201-acafc136b7e8")
                .type(Income.IncomeType.JOB)
                .frequency(PaymentFrequency.MONTH)
                .amount(AMOUNT)
                .build()
            ))
            .partner(LivingPartner.builder()
                .over18(true)
                .disability(DisabilityStatus.NO)
                .build())
            .disability(DisabilityStatus.YES)
            .carer(false)
            .build();
    }

}

package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChildren;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDResidenceType;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDStatementOfMeans;
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
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatementOfMeansMapperTest {

    @Autowired
    private StatementOfMeansMapper mapper;

    @Test
    public void shouldMapStatementOfMeansToCCD() {
        //given
        StatementOfMeans statementOfMeans = StatementOfMeans.builder()
            .residenceType(ResidenceType.OWN_HOME)
            .reason("My reason")
            .bankAccounts(asList(BankAccount.builder()
                .typeOfAccount(BankAccount.BankAccountType.SAVING_ACCOUNT)
                .isJoint(YesNoOption.YES)
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
                .isEmployed(YesNoOption.YES)
                .employers(asList(Employer.builder().employerName("CMC").jobTitle("My sweet job").build()))
                .isSelfEmployed(YesNoOption.NO)
                .build()
            )
            .build();
        //when
        CCDStatementOfMeans ccdStatementOfMeans = mapper.to(statementOfMeans);

        //then
        assertThat(statementOfMeans).isEqualTo(ccdStatementOfMeans);
    }

    @Test
    public void shouldMapStatementOfMeansFromCCD() {
        //given
        CCDStatementOfMeans ccdStatementOfMeans = CCDStatementOfMeans.builder()
            .residenceType(CCDResidenceType.OWN_HOME)
            .reason("My reason")
            .dependant(CCDDependant.builder()
                .maintainedChildren(1)
                .children(CCDChildren.builder().between16and19(2).between11and15(1).under11(0).build())
                .build()
            )
            .employment(CCDEmployment.builder()
                .employers(asList(
                    CCDCollectionElement.<CCDEmployer>builder().value(CCDEmployer.builder()
                        .jobTitle("A job")
                        .employerName("A Company")
                        .build()
                    ).build()
                ))
                .isEmployed(CCDYesNoOption.YES)
                .isSelfEmployed(CCDYesNoOption.NO)
                .build()
            )
            .incomes(asList(
                CCDCollectionElement.<CCDIncome>builder().value(CCDIncome.builder()
                    .type("Salary")
                    .frequency(CCDPaymentFrequency.MONTH)
                    .amountReceived(BigDecimal.TEN)
                    .build()
                ).build()
            ))
            .expenses(asList(
                CCDCollectionElement.<CCDExpense>builder().value(CCDExpense.builder()
                    .type("Salary")
                    .frequency(CCDPaymentFrequency.MONTH)
                    .amountPaid(BigDecimal.TEN)
                    .build()
                ).build()
            ))
            .debts(asList(
                CCDCollectionElement.<CCDDebt>builder().value(CCDDebt.builder()
                    .totalOwed(BigDecimal.TEN)
                    .description("Reference")
                    .monthlyPayments(BigDecimal.ONE)
                    .build()
                ).build()
            ))
            .bankAccounts(asList(
                CCDCollectionElement.<CCDBankAccount>builder().value(CCDBankAccount.builder()
                    .balance(BigDecimal.valueOf(100))
                    .isJoint(CCDYesNoOption.NO)
                    .typeOfAccount(CCDBankAccount.BankAccountType.SAVING_ACCOUNT)
                    .build()
                ).build()
            ))
            .courtOrders(asList(
                CCDCollectionElement.<CCDCourtOrder>builder().value(CCDCourtOrder.builder().build().builder()
                    .amountOwed(BigDecimal.TEN)
                    .details("Reference")
                    .monthlyInstalmentAmount(BigDecimal.ONE)
                    .build()
                ).build()
            ))
            .build();

        //when
        StatementOfMeans statementOfMeans = mapper.from(ccdStatementOfMeans);

        //then
        assertThat(statementOfMeans).isEqualTo(ccdStatementOfMeans);
    }
}

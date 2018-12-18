package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class StatementOfMeansMapper implements Mapper<CCDStatementOfMeans, StatementOfMeans> {

    private final BankAccountMapper bankAccountMapper;
    private final DebtMapper debtMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final CourtOrderMapper courtOrderMapper;
    private final EmploymentMapper employmentMapper;
    private final ChildCategoryMapper childCategoryMapper;

    @Autowired
    public StatementOfMeansMapper(
        BankAccountMapper bankAccountMapper,
        DebtMapper debtMapper,
        IncomeMapper incomeMapper,
        ExpenseMapper expenseMapper,
        CourtOrderMapper courtOrderMapper,
        EmploymentMapper employmentMapper,
        ChildCategoryMapper childCategoryMapper
    ) {
        this.bankAccountMapper = bankAccountMapper;
        this.debtMapper = debtMapper;
        this.incomeMapper = incomeMapper;
        this.expenseMapper = expenseMapper;
        this.courtOrderMapper = courtOrderMapper;
        this.employmentMapper = employmentMapper;
        this.childCategoryMapper = childCategoryMapper;
    }

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        CCDStatementOfMeans.CCDStatementOfMeansBuilder builder = CCDStatementOfMeans.builder();

        builder.reason(statementOfMeans.getReason());
        builder.residenceType(statementOfMeans.getResidence().getType());
        statementOfMeans.getResidence().getOtherDetail().ifPresent(builder::residenceOtherDetail);

        statementOfMeans.getDependant().ifPresent(dependant -> {
            builder.noOfMaintainedChildren(dependant.getNumberOfMaintainedChildren().orElse(0));
            builder.anyDisabledChildren(dependant.isAnyDisabledChildren() ? YES : NO);

            dependant.getOtherDependants().ifPresent(otherDependants -> {
                builder.numberOfOtherDependants(otherDependants.getNumberOfPeople());
                builder.otherDependantDetails(otherDependants.getDetails());
            });

            builder.dependantChildren(
                asStream(dependant.getChildren())
                    .map(childCategoryMapper::to)
                    .filter(Objects::nonNull)
                    .map(childCategory -> CCDCollectionElement.<CCDChildCategory>builder().value(childCategory).build())
                    .collect(Collectors.toList())
            );

        });

        statementOfMeans.getEmployment().ifPresent(employment -> {
            employment.getUnemployment().ifPresent(unemployment -> {
                unemployment.getUnemployed().ifPresent(unemployed -> {
                    builder.unEmployedNoOfYears(unemployed.getNumberOfYears());
                    builder.unEmployedNoOfMonths(unemployed.getNumberOfMonths());
                });
                unemployment.getOther().ifPresent(builder::employmentDetails);
            });

            employment.getSelfEmployment().ifPresent(selfEmployment -> {
                builder.selfEmploymentJobTitle(selfEmployment.getJobTitle());
                builder.selfEmploymentAnnualTurnover(selfEmployment.getAnnualTurnover());

                selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments -> {
                    builder.taxYouOwe(onTaxPayments.getAmountYouOwe());
                    builder.taxPaymentsReason(onTaxPayments.getReason());
                });
            });

            builder.employers(
                asStream(employment.getEmployers())
                    .map(employmentMapper::to)
                    .filter(Objects::nonNull)
                    .map(employer -> CCDCollectionElement.<CCDEmployment>builder().value(employer).build())
                    .collect(Collectors.toList()));


        });

        builder.bankAccounts(
            asStream(statementOfMeans.getBankAccounts())
                .map(bankAccountMapper::to)
                .filter(Objects::nonNull)
                .map(bankAccount -> CCDCollectionElement.<CCDBankAccount>builder().value(bankAccount).build())
                .collect(Collectors.toList()));

        builder.debts(
            statementOfMeans.getDebts()
                .stream()
                .map(debtMapper::to)
                .filter(Objects::nonNull)
                .map(debt -> CCDCollectionElement.<CCDDebt>builder().value(debt).build())
                .collect(Collectors.toList())
        );

        builder.incomes(statementOfMeans.getIncomes()
            .stream()
            .map(incomeMapper::to)
            .filter(Objects::nonNull)
            .map(income -> CCDCollectionElement.<CCDIncome>builder().value(income).build())
            .collect(Collectors.toList())
        );

        builder.expenses(
            statementOfMeans.getExpenses()
                .stream()
                .map(expenseMapper::to)
                .filter(Objects::nonNull)
                .map(expense -> CCDCollectionElement.<CCDExpense>builder().value(expense).build())
                .collect(Collectors.toList())
        );

        builder.courtOrders(
            statementOfMeans.getCourtOrders()
                .stream()
                .map(courtOrderMapper::to)
                .filter(Objects::nonNull)
                .map(courtOrder -> CCDCollectionElement.<CCDCourtOrder>builder().value(courtOrder).build())
                .collect(Collectors.toList())
        );

        builder.priorityDebts(
            statementOfMeans.getPriorityDebts()
                .stream()
                .filter(Objects::nonNull)
                .map(priorityDebt -> CCDCollectionElement.<PriorityDebt>builder().value(priorityDebt).build())
                .collect(Collectors.toList())
        );

        builder.carer(statementOfMeans.isCarer() ? YES : NO);
        statementOfMeans.getDisability().ifPresent(builder::disabilityStatus);
        statementOfMeans.getPartner().ifPresent(builder::livingPartner);

        return builder.build();
    }

    @Override
    public StatementOfMeans from(CCDStatementOfMeans ccdStatementOfMeans) {
        if (ccdStatementOfMeans == null) {
            return null;
        }
        //TODO: flattened  fields to map back.

        List<BankAccount> bankAccounts = asStream(ccdStatementOfMeans.getBankAccounts())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(bankAccountMapper::from)
            .collect(Collectors.toList());

        List<Debt> debts = asStream(ccdStatementOfMeans.getDebts())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(debtMapper::from)
            .collect(Collectors.toList());

        List<Income> incomes = asStream(ccdStatementOfMeans.getIncomes())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(incomeMapper::from)
            .collect(Collectors.toList());

        List<Expense> expenses = asStream(ccdStatementOfMeans.getExpenses())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(expenseMapper::from)
            .collect(Collectors.toList());

        List<CourtOrder> courtOrders = asStream(ccdStatementOfMeans.getCourtOrders())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(courtOrderMapper::from)
            .collect(Collectors.toList());

        List<PriorityDebt> priorityDebts = asStream(ccdStatementOfMeans.getPriorityDebts())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return StatementOfMeans.builder()

            .bankAccounts(bankAccounts)
            .debts(debts)
            .incomes(incomes)
            .expenses(expenses)
            .courtOrders(courtOrders)
            .priorityDebts(priorityDebts)
            .reason(ccdStatementOfMeans.getReason())
            .build();
    }
}

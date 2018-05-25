package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDResidenceType;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.ResidenceType;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StatementOfMeansMapper implements Mapper<CCDStatementOfMeans, StatementOfMeans> {

    private final DependantMapper dependantMapper;
    private final EmploymentMapper employmentMapper;
    private final BankAccountMapper bankAccountMapper;
    private final DebtMapper debtMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final CourtOrderMapper courtOrderMapper;

    @Autowired
    public StatementOfMeansMapper(
        DependantMapper dependantMapper,
        EmploymentMapper employmentMapper,
        BankAccountMapper bankAccountMapper,
        DebtMapper debtMapper,
        IncomeMapper incomeMapper,
        ExpenseMapper expenseMapper,
        CourtOrderMapper courtOrderMapper
    ) {
        this.dependantMapper = dependantMapper;
        this.employmentMapper = employmentMapper;
        this.bankAccountMapper = bankAccountMapper;
        this.debtMapper = debtMapper;
        this.incomeMapper = incomeMapper;
        this.expenseMapper = expenseMapper;
        this.courtOrderMapper = courtOrderMapper;
    }

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        CCDStatementOfMeans.CCDStatementOfMeansBuilder builder = CCDStatementOfMeans.builder()
            .residenceType(CCDResidenceType.valueOf(statementOfMeans.getResidenceType().name()))
            .reason(statementOfMeans.getReason());

        statementOfMeans.getDependant()
            .ifPresent(dependant -> builder.dependant(dependantMapper.to(dependant)));

        statementOfMeans.getEmployment()
            .ifPresent(employment -> builder.employment(employmentMapper.to(employment)));

        builder.bankAccounts(
            statementOfMeans.getBankAccounts()
                .stream()
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
                .collect(Collectors.toList()));

        builder.incomes(
            statementOfMeans.getIncomes()
                .stream()
                .map(incomeMapper::to)
                .filter(Objects::nonNull)
                .map(incom -> CCDCollectionElement.<CCDIncome>builder().value(incom).build())
                .collect(Collectors.toList()));

        builder.expenses(
            statementOfMeans.getExpenses()
                .stream()
                .map(expenseMapper::to)
                .filter(Objects::nonNull)
                .map(expense -> CCDCollectionElement.<CCDExpense>builder().value(expense).build())
                .collect(Collectors.toList()));

        builder.courtOrders(
            statementOfMeans.getCourtOrders()
                .stream()
                .map(courtOrderMapper::to)
                .filter(Objects::nonNull)
                .map(courtOrder -> CCDCollectionElement.<CCDCourtOrder>builder().value(courtOrder).build())
                .collect(Collectors.toList()));

        return builder.build();
    }

    @Override
    public StatementOfMeans from(CCDStatementOfMeans ccdStatementOfMeans) {
        if (ccdStatementOfMeans == null) {
            return null;
        }

        List<BankAccount> bankAccounts = ccdStatementOfMeans.getBankAccounts()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(bankAccountMapper::from)
            .collect(Collectors.toList());

        List<Debt> debts = ccdStatementOfMeans.getDebts()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(debtMapper::from)
            .collect(Collectors.toList());

        List<Income> incomes = ccdStatementOfMeans.getIncomes()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(incomeMapper::from)
            .collect(Collectors.toList());

        List<Expense> expenses = ccdStatementOfMeans.getExpenses()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(expenseMapper::from)
            .collect(Collectors.toList());

        List<CourtOrder> courtOrders = ccdStatementOfMeans.getCourtOrders()
            .stream()
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(courtOrderMapper::from)
            .collect(Collectors.toList());

        return new StatementOfMeans(
            ResidenceType.valueOf(ccdStatementOfMeans.getResidenceType().name()),
            dependantMapper.from(ccdStatementOfMeans.getDependant()),
            employmentMapper.from(ccdStatementOfMeans.getEmployment()),
            bankAccounts,
            debts,
            incomes,
            expenses,
            courtOrders,
            ccdStatementOfMeans.getReason()

        );
    }
}

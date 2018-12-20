package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDResidence;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class StatementOfMeansMapper implements Mapper<CCDStatementOfMeans, StatementOfMeans> {

    private final DependantMapper dependantMapper;
    private final EmploymentMapper employmentMapper;
    private final BankAccountMapper bankAccountMapper;
    private final DebtMapper debtMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final CourtOrderMapper courtOrderMapper;
    private final LivingPartnerMapper livingPartnerMapper;

    @Autowired
    public StatementOfMeansMapper(
        DependantMapper dependantMapper,
        EmploymentMapper employmentMapper,
        BankAccountMapper bankAccountMapper,
        DebtMapper debtMapper,
        IncomeMapper incomeMapper,
        ExpenseMapper expenseMapper,
        CourtOrderMapper courtOrderMapper,
        LivingPartnerMapper livingPartnerMapper) {
        this.dependantMapper = dependantMapper;
        this.employmentMapper = employmentMapper;
        this.bankAccountMapper = bankAccountMapper;
        this.debtMapper = debtMapper;
        this.incomeMapper = incomeMapper;
        this.expenseMapper = expenseMapper;
        this.courtOrderMapper = courtOrderMapper;
        this.livingPartnerMapper = livingPartnerMapper;
    }

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        CCDStatementOfMeans.CCDStatementOfMeansBuilder builder = CCDStatementOfMeans.builder()
            .residence(
                CCDResidence.builder()
                    .otherDetail(statementOfMeans.getResidence().getOtherDetail().orElse(null))
                    .type(statementOfMeans.getResidence().getType())
                    .build()
            )
            .reason(statementOfMeans.getReason());

        statementOfMeans.getDependant()
            .ifPresent(dependant -> builder.dependant(dependantMapper.to(dependant)));

        statementOfMeans.getEmployment()
            .ifPresent(employment -> builder.employment(employmentMapper.to(employment)));

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

        statementOfMeans.getPartner().ifPresent(partner -> builder.partner(livingPartnerMapper.to(partner)));
        statementOfMeans.getDisability().ifPresent(builder::disability);
        builder.carer(CCDYesNoOption.valueOf(statementOfMeans.isCarer()));

        return builder.build();
    }

    @Override
    public StatementOfMeans from(CCDStatementOfMeans ccdStatementOfMeans) {
        if (ccdStatementOfMeans == null) {
            return null;
        }

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

        Residence residence = Residence.builder()
            .type(ccdStatementOfMeans.getResidence().getType())
            .otherDetail(ccdStatementOfMeans.getResidence().getOtherDetail())
            .build();

        return StatementOfMeans.builder()
            .residence(residence)
            .dependant(dependantMapper.from(ccdStatementOfMeans.getDependant()))
            .employment(employmentMapper.from(ccdStatementOfMeans.getEmployment()))
            .bankAccounts(bankAccounts)
            .debts(debts)
            .incomes(incomes)
            .expenses(expenses)
            .courtOrders(courtOrders)
            .priorityDebts(priorityDebts)
            .reason(ccdStatementOfMeans.getReason())
            .partner(livingPartnerMapper.from(ccdStatementOfMeans.getPartner()))
            .disability(ccdStatementOfMeans.getDisability())
            .carer(Optional.ofNullable(ccdStatementOfMeans.getCarer()).orElse(CCDYesNoOption.NO).toBoolean())
            .build();
    }
}

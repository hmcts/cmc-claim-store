package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployed;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.isBlank;
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
    private final EmployerMapper employerMapper;
    private final ChildCategoryMapper childCategoryMapper;
    private final LivingPartnerMapper livingPartnerMapper;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public StatementOfMeansMapper(
        BankAccountMapper bankAccountMapper,
        DebtMapper debtMapper,
        IncomeMapper incomeMapper,
        ExpenseMapper expenseMapper,
        CourtOrderMapper courtOrderMapper,
        EmployerMapper employerMapper,
        ChildCategoryMapper childCategoryMapper,
        LivingPartnerMapper livingPartnerMapper
    ) {
        this.bankAccountMapper = bankAccountMapper;
        this.debtMapper = debtMapper;
        this.incomeMapper = incomeMapper;
        this.expenseMapper = expenseMapper;
        this.courtOrderMapper = courtOrderMapper;
        this.employerMapper = employerMapper;
        this.childCategoryMapper = childCategoryMapper;
        this.livingPartnerMapper = livingPartnerMapper;
    }

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        CCDStatementOfMeans.CCDStatementOfMeansBuilder builder = CCDStatementOfMeans.builder();

        builder.reason(statementOfMeans.getReason());
        builder.residenceType(statementOfMeans.getResidence().getType());
        statementOfMeans.getResidence().getOtherDetail().ifPresent(builder::residenceOtherDetail);

        statementOfMeans.getDependant().ifPresent(toDependantConsumer(builder));

        statementOfMeans.getEmployment().ifPresent(toEmploymentConsumer(builder));

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

        builder.carer(CCDYesNoOption.valueOf(statementOfMeans.isCarer()));
        statementOfMeans.getDisability().ifPresent(builder::disabilityStatus);

        statementOfMeans.getPartner().ifPresent(partner -> builder.livingPartner(livingPartnerMapper.to(partner)));

        return builder.build();
    }

    private Consumer<Employment> toEmploymentConsumer(CCDStatementOfMeans.CCDStatementOfMeansBuilder builder) {
        return employment -> {
            employment.getUnemployment().ifPresent(toUnemploymentConsumer(builder));

            employment.getSelfEmployment().ifPresent(toSelfEmploymentConsumer(builder));

            builder.employers(
                asStream(employment.getEmployers())
                    .map(employerMapper::to)
                    .filter(Objects::nonNull)
                    .map(employer -> CCDCollectionElement.<CCDEmployer>builder().value(employer).build())
                    .collect(Collectors.toList()));
        };
    }

    private Consumer<SelfEmployment> toSelfEmploymentConsumer(CCDStatementOfMeans.CCDStatementOfMeansBuilder builder) {
        return selfEmployment -> {
            builder.selfEmploymentJobTitle(selfEmployment.getJobTitle());
            builder.selfEmploymentAnnualTurnover(selfEmployment.getAnnualTurnover());

            selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments -> {
                builder.taxYouOwe(onTaxPayments.getAmountYouOwe());
                builder.taxPaymentsReason(onTaxPayments.getReason());
            });
        };
    }

    private Consumer<Unemployment> toUnemploymentConsumer(CCDStatementOfMeans.CCDStatementOfMeansBuilder builder) {
        return unemployment -> {
            unemployment.getUnemployed().ifPresent(unemployed -> {
                builder.unEmployedNoOfYears(unemployed.getNumberOfYears());
                builder.unEmployedNoOfMonths(unemployed.getNumberOfMonths());
            });
            unemployment.getOther().ifPresent(builder::employmentDetails);
            builder.retired(CCDYesNoOption.valueOf(unemployment.isRetired()));
        };
    }

    private Consumer<Dependant> toDependantConsumer(CCDStatementOfMeans.CCDStatementOfMeansBuilder builder) {
        return dependant -> {
            builder.noOfMaintainedChildren(dependant.getNumberOfMaintainedChildren().orElse(0));
            builder.anyDisabledChildren(CCDYesNoOption.valueOf(dependant.isAnyDisabledChildren()));

            dependant.getOtherDependants().ifPresent(otherDependants -> {
                builder.numberOfOtherDependants(otherDependants.getNumberOfPeople());
                builder.otherDependantDetails(otherDependants.getDetails());
                builder.otherDependantAnyDisabled(otherDependants.isAnyDisabled() ? YES : NO);
            });

            builder.dependantChildren(
                asStream(dependant.getChildren())
                    .map(childCategoryMapper::to)
                    .filter(Objects::nonNull)
                    .map(childCategory -> CCDCollectionElement.<CCDChildCategory>builder().value(childCategory).build())
                    .collect(Collectors.toList())
            );
        };
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

        return StatementOfMeans.builder()
            .reason(ccdStatementOfMeans.getReason())
            .residence(extractResidence(ccdStatementOfMeans))
            .dependant(extractDependant(ccdStatementOfMeans))
            .employment(extractEmployment(ccdStatementOfMeans))
            .bankAccounts(bankAccounts)
            .debts(debts)
            .incomes(incomes)
            .expenses(expenses)
            .courtOrders(courtOrders)
            .priorityDebts(priorityDebts)
            .partner(livingPartnerMapper.from(ccdStatementOfMeans.getLivingPartner()))
            .disability(ccdStatementOfMeans.getDisabilityStatus())
            .carer(ccdStatementOfMeans.getCarer().toBoolean())
            .build();
    }

    private Employment extractEmployment(CCDStatementOfMeans ccdStatementOfMeans) {

        List<Employer> employers = asStream(ccdStatementOfMeans.getEmployers())
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(employerMapper::from)
            .collect(Collectors.toList());

        return Employment.builder()
            .selfEmployment(extractSelfEmployment(ccdStatementOfMeans))
            .unemployment(extractUnemployment(ccdStatementOfMeans))
            .employers(employers)
            .build();
    }

    private Unemployment extractUnemployment(CCDStatementOfMeans ccdStatementOfMeans) {
        return Unemployment.builder()
            .retired(ccdStatementOfMeans.getRetired().toBoolean())
            .other(ccdStatementOfMeans.getEmploymentDetails())
            .unemployed(extractUnemployed(ccdStatementOfMeans))
            .build();
    }

    @Valid
    private Unemployed extractUnemployed(CCDStatementOfMeans ccdStatementOfMeans) {
        Integer noOfMonths = ccdStatementOfMeans.getUnEmployedNoOfMonths();
        Integer noOfYears = ccdStatementOfMeans.getUnEmployedNoOfYears();
        if (noOfMonths == null && noOfYears == null) {
            return null;
        }

        return Unemployed.builder()
            .numberOfMonths(noOfMonths)
            .numberOfYears(noOfYears)
            .build();
    }

    private SelfEmployment extractSelfEmployment(CCDStatementOfMeans ccdStatementOfMeans) {
        String jobTitle = ccdStatementOfMeans.getSelfEmploymentJobTitle();
        BigDecimal annualTurnover = ccdStatementOfMeans.getSelfEmploymentAnnualTurnover();
        if (isBlank(jobTitle) && annualTurnover == null) {
            return null;
        }
        return SelfEmployment.builder()
            .jobTitle(jobTitle)
            .annualTurnover(annualTurnover)
            .onTaxPayments(extractOnTaxPayments(ccdStatementOfMeans))
            .build();
    }

    private OnTaxPayments extractOnTaxPayments(CCDStatementOfMeans ccdStatementOfMeans) {
        BigDecimal taxYouOwe = ccdStatementOfMeans.getTaxYouOwe();
        String reason = ccdStatementOfMeans.getTaxPaymentsReason();
        if (isBlank(reason) && taxYouOwe == null) {
            return null;
        }

        return OnTaxPayments.builder()
            .amountYouOwe(taxYouOwe)
            .reason(reason)
            .build();
    }

    private Dependant extractDependant(CCDStatementOfMeans ccdStatementOfMeans) {
        return Dependant.builder()
            .numberOfMaintainedChildren(ccdStatementOfMeans.getNoOfMaintainedChildren())
            .otherDependants(extractOtherDependants(ccdStatementOfMeans))
            .anyDisabledChildren(ccdStatementOfMeans.getAnyDisabledChildren().toBoolean())
            .children(extractChildren(ccdStatementOfMeans.getDependantChildren()))
            .build();
    }

    private List<Child> extractChildren(List<CCDCollectionElement<CCDChildCategory>> dependantChildren) {
        return asStream(dependantChildren)
            .map(CCDCollectionElement::getValue)
            .filter(Objects::nonNull)
            .map(childCategoryMapper::from)
            .collect(Collectors.toList());
    }

    private OtherDependants extractOtherDependants(CCDStatementOfMeans ccdStatementOfMeans) {
        String details = ccdStatementOfMeans.getOtherDependantDetails();
        Integer numberOfPeople = ccdStatementOfMeans.getNumberOfOtherDependants();
        if (numberOfPeople == null && isBlank(details)) {
            return null;
        }
        return OtherDependants.builder()
            .details(details)
            .numberOfPeople(numberOfPeople)
            .anyDisabled(ccdStatementOfMeans.getOtherDependantAnyDisabled().toBoolean())
            .build();
    }

    private Residence extractResidence(CCDStatementOfMeans ccdStatementOfMeans) {
        return Residence.builder()
            .type(ccdStatementOfMeans.getResidenceType())
            .otherDetail(ccdStatementOfMeans.getResidenceOtherDetail())
            .build();
    }
}

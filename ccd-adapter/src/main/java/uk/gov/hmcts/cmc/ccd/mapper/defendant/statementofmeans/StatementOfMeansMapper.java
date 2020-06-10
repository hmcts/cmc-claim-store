package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDisabilityStatus;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDResidenceType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
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
    private final PriorityDebtMapper priorityDebtMapper;
    private final MoneyMapper moneyMapper;

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
        LivingPartnerMapper livingPartnerMapper,
        PriorityDebtMapper priorityDebtMapper,
        MoneyMapper moneyMapper
    ) {
        this.bankAccountMapper = bankAccountMapper;
        this.debtMapper = debtMapper;
        this.incomeMapper = incomeMapper;
        this.expenseMapper = expenseMapper;
        this.courtOrderMapper = courtOrderMapper;
        this.employerMapper = employerMapper;
        this.childCategoryMapper = childCategoryMapper;
        this.livingPartnerMapper = livingPartnerMapper;
        this.priorityDebtMapper = priorityDebtMapper;
        this.moneyMapper = moneyMapper;
    }

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        CCDStatementOfMeans.CCDStatementOfMeansBuilder builder = CCDStatementOfMeans.builder();

        builder.reason(statementOfMeans.getReason());
        builder.residenceType(CCDResidenceType.valueOf(statementOfMeans.getResidence().getType().name()));
        statementOfMeans.getResidence().getOtherDetail().ifPresent(builder::residenceOtherDetail);

        statementOfMeans.getDependant().ifPresent(toDependantConsumer(builder));

        statementOfMeans.getEmployment().ifPresent(toEmploymentConsumer(builder));

        builder.bankAccounts(
            asStream(statementOfMeans.getBankAccounts())
                .map(bankAccountMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        builder.debts(
            statementOfMeans.getDebts()
                .stream()
                .map(debtMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        builder.incomes(statementOfMeans.getIncomes()
            .stream()
            .map(incomeMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        builder.expenses(
            statementOfMeans.getExpenses()
                .stream()
                .map(expenseMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        builder.courtOrders(
            statementOfMeans.getCourtOrders()
                .stream()
                .map(courtOrderMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        builder.priorityDebts(
            statementOfMeans.getPriorityDebts()
                .stream()
                .map(priorityDebtMapper::to)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        builder.carer(CCDYesNoOption.valueOf(statementOfMeans.isCarer()));
        statementOfMeans.getDisability()
            .ifPresent(disability -> builder.disabilityStatus(CCDDisabilityStatus.valueOf(disability.name())));

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
                    .collect(Collectors.toList()));
        };
    }

    private Consumer<SelfEmployment> toSelfEmploymentConsumer(CCDStatementOfMeans.CCDStatementOfMeansBuilder builder) {
        return selfEmployment -> {
            builder.selfEmploymentJobTitle(selfEmployment.getJobTitle());
            builder.selfEmploymentAnnualTurnover(moneyMapper.to(selfEmployment.getAnnualTurnover()));

            selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments -> {
                builder.taxYouOwe(moneyMapper.to(onTaxPayments.getAmountYouOwe()));
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
            .map(bankAccountMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<Debt> debts = asStream(ccdStatementOfMeans.getDebts())
            .map(debtMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<Income> incomes = asStream(ccdStatementOfMeans.getIncomes())
            .map(incomeMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<Expense> expenses = asStream(ccdStatementOfMeans.getExpenses())
            .map(expenseMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<CourtOrder> courtOrders = asStream(ccdStatementOfMeans.getCourtOrders())
            .map(courtOrderMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<PriorityDebt> priorityDebts = asStream(ccdStatementOfMeans.getPriorityDebts())
            .map(priorityDebtMapper::from)
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
            .disability(ccdStatementOfMeans.getDisabilityStatus() == null ? null :
                DisabilityStatus.valueOf(ccdStatementOfMeans.getDisabilityStatus().name()))
            .carer(ccdStatementOfMeans.getCarer() != null && ccdStatementOfMeans.getCarer().toBoolean())
            .build();
    }

    private Employment extractEmployment(CCDStatementOfMeans ccdStatementOfMeans) {

        List<Employer> employers = asStream(ccdStatementOfMeans.getEmployers())
            .map(employerMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return Employment.builder()
            .selfEmployment(extractSelfEmployment(ccdStatementOfMeans))
            .unemployment(extractUnemployment(ccdStatementOfMeans))
            .employers(employers)
            .build();
    }

    private Unemployment extractUnemployment(CCDStatementOfMeans ccdStatementOfMeans) {
        CCDYesNoOption retired = ccdStatementOfMeans.getRetired();
        String employmentDetails = ccdStatementOfMeans.getEmploymentDetails();
        Unemployed unemployed = extractUnemployed(ccdStatementOfMeans);
        if (retired == null && employmentDetails == null && unemployed == null) {
            return null;
        }
        return Unemployment.builder()
            .retired(retired != null && retired.toBoolean())
            .other(employmentDetails)
            .unemployed(unemployed)
            .build();
    }

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
        BigDecimal annualTurnover = moneyMapper.from(ccdStatementOfMeans.getSelfEmploymentAnnualTurnover());
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
        BigDecimal taxYouOwe = moneyMapper.from(ccdStatementOfMeans.getTaxYouOwe());
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
            .anyDisabledChildren(ccdStatementOfMeans.getAnyDisabledChildren() != null
                && ccdStatementOfMeans.getAnyDisabledChildren().toBoolean()
            )
            .children(extractChildren(ccdStatementOfMeans.getDependantChildren()))
            .build();
    }

    private List<Child> extractChildren(List<CCDCollectionElement<CCDChildCategory>> dependantChildren) {
        return asStream(dependantChildren)
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
            .anyDisabled(ccdStatementOfMeans.getOtherDependantAnyDisabled() != null
                && ccdStatementOfMeans.getOtherDependantAnyDisabled().toBoolean()
            )
            .build();
    }

    private Residence extractResidence(CCDStatementOfMeans ccdStatementOfMeans) {
        if (ccdStatementOfMeans.getResidenceType() == null) {
            return null;
        }
        return Residence.builder()
            .type(Residence.ResidenceType.valueOf(ccdStatementOfMeans.getResidenceType().name()))
            .otherDetail(ccdStatementOfMeans.getResidenceOtherDetail())
            .build();
    }
}

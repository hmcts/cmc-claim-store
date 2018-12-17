package uk.gov.hmcts.cmc.ccd.deprecated;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponseType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDResidence;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount.BankAccountType.SAVINGS_ACCOUNT;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.COUNCIL_TAX;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.JOB;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt.PriorityDebtType.ELECTRICITY;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence.ResidenceType.JOINT_OWN_HOME;

public class SampleData {

    //Utility class
    private SampleData() {
    }


    public static CCDStatementOfTruth getCCDStatementOfTruth() {
        return CCDStatementOfTruth
            .builder()
            .signerName("name")
            .signerRole("role")
            .build();
    }

    public static CCDClaimantResponse getCCDClaimantREjectionResponse() {
        return CCDClaimantResponse.builder()
            .claimantResponseType(CCDClaimantResponseType.REJECTION)
            .responseRejection(getResponseRejection())
            .build();
    }


    public static CCDResponseRejection getResponseRejection() {
        return CCDResponseRejection.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .freeMediationOption(CCDYesNoOption.YES)
            .reason("Rejection Reason")
            .build();
    }

    public static CCDStatementOfMeans getCCDStatementOfMeans() {
        return CCDStatementOfMeans.builder()
            .residence(CCDResidence.builder().type(JOINT_OWN_HOME).build())
            .reason("My reason")
            .dependant(CCDDependant.builder()
                .children(asList(CCDCollectionElement.<CCDChild>builder()
                    .value(CCDChild.builder()
                        .numberOfChildren(4)
                        .numberOfChildrenLivingWithYou(1)
                        .ageGroupType(Child.AgeGroupType.BETWEEN_11_AND_15)
                        .build())
                    .build())
                )
                .build()
            )
            .employment(CCDEmployment.builder()
                .employers(asList(
                    CCDCollectionElement.<CCDEmployer>builder().value(CCDEmployer.builder()
                        .jobTitle("A job")
                        .name("A Company")
                        .build()
                    ).build()
                ))
                .build()
            )
            .incomes(asList(
                CCDCollectionElement.<CCDIncome>builder().value(CCDIncome.builder()
                    .type(JOB)
                    .frequency(MONTH)
                    .amountReceived(TEN)
                    .build()
                ).build()
            ))
            .expenses(asList(
                CCDCollectionElement.<CCDExpense>builder().value(CCDExpense.builder()
                    .type(COUNCIL_TAX)
                    .frequency(MONTH)
                    .amountPaid(TEN)
                    .build()
                ).build()
            ))
            .debts(asList(
                CCDCollectionElement.<CCDDebt>builder().value(CCDDebt.builder()
                    .totalOwed(TEN)
                    .description("Reference")
                    .monthlyPayments(ONE)
                    .build()
                ).build()
            ))
            .bankAccounts(asList(
                CCDCollectionElement.<CCDBankAccount>builder().value(CCDBankAccount.builder()
                    .balance(BigDecimal.valueOf(100))
                    .joint(NO)
                    .type(SAVINGS_ACCOUNT)
                    .build()
                ).build()
            ))
            .courtOrders(asList(
                CCDCollectionElement.<CCDCourtOrder>builder().value(CCDCourtOrder.builder()
                    .amountOwed(TEN)
                    .claimNumber("Reference")
                    .monthlyInstalmentAmount(ONE)
                    .build()
                ).build()
            ))
            .priorityDebts(asList(
                CCDCollectionElement.<PriorityDebt>builder().value(PriorityDebt.builder()
                    .frequency(MONTH)
                    .amount(BigDecimal.valueOf(132.89))
                    .type(ELECTRICITY)
                    .build()
                ).build()
            ))
            .carer(CCDYesNoOption.YES)
            .partner(CCDLivingPartner.builder()
                .disability(DisabilityStatus.SEVERE)
                .over18(CCDYesNoOption.YES)
                .pensioner(CCDYesNoOption.YES)
                .build()
            )
            .disability(DisabilityStatus.YES)
            .build();
    }
}

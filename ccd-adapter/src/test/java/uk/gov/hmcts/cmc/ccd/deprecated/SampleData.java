package uk.gov.hmcts.cmc.ccd.deprecated;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDClaim;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDHousingDisrepair;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPayment;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPersonalInjury;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponseType;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullDefenceResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDResidence;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.RANGE;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.SOLE_TRADER;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;
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

    public static CCDIndividual getCCDIndividual() {
        CCDAddress ccdAddress = getCCDAddress();
        CCDContactDetails ccdContactDetails = getCCDContactDetails();
        CCDRepresentative ccdRepresentative = getCCDRepresentative(ccdAddress, ccdContactDetails);

        return CCDIndividual.builder()
            .name("Individual")
            .phoneNumber("07987654321")
            .dateOfBirth("1950-01-01")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();
    }

    public static CCDContactDetails getCCDContactDetails() {
        return CCDContactDetails.builder()
            .phone("07987654321")
            .email(",my@email.com")
            .dxAddress("dx123")
            .build();
    }

    public static CCDAddress getCCDAddress() {
        return CCDAddress.builder()
            .line1("line1")
            .line2("line2")
            .line3("line3")
            .city("city")
            .postcode("postcode")
            .build();
    }

    public static CCDCompany getCCDCompany() {
        CCDAddress ccdAddress = getCCDAddress();
        CCDContactDetails ccdContactDetails = getCCDContactDetails();
        CCDRepresentative ccdRepresentative = getCCDRepresentative(ccdAddress, ccdContactDetails);
        return CCDCompany.builder()
            .name("Abc Ltd")
            .address(ccdAddress)
            .phoneNumber("07987654321")
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .contactPerson("MR. Hyde")
            .build();
    }

    public static CCDRepresentative getCCDRepresentative(CCDAddress ccdAddress, CCDContactDetails ccdContactDetails) {
        return CCDRepresentative
            .builder()
            .organisationName("My Org")
            .organisationContactDetails(ccdContactDetails)
            .organisationAddress(ccdAddress)
            .build();
    }

    public static CCDParty getCCDPartyIndividual() {
        return CCDParty.builder()
            .type(INDIVIDUAL)
            .individual(getCCDIndividual())
            .build();
    }

    public static CCDParty getCCDPartyCompany() {
        return CCDParty.builder()
            .type(COMPANY)
            .company(getCCDCompany())
            .build();
    }

    public static CCDParty getCCDPartyOrganisation() {
        return CCDParty.builder()
            .type(ORGANISATION)
            .organisation(getCCDOrganisation())
            .build();
    }

    public static CCDParty getCCDPartySoleTrader() {
        return CCDParty.builder()
            .type(SOLE_TRADER)
            .soleTrader(getCCDSoleTrader())
            .build();
    }

    public static CCDClaim getCCDLegalClaim() {
        return CCDClaim.builder()
            .amount(
                CCDAmount.builder()
                    .type(RANGE)
                    .amountRange(
                        CCDAmountRange.builder()
                            .lowerValue(BigDecimal.valueOf(50))
                            .higherValue(BigDecimal.valueOf(500))
                            .build()).build())
            .housingDisrepair(getCCDHousingDisrepair())
            .personalInjury(getCCDPersonalInjury())
            .statementOfTruth(getCCDStatementOfTruth())
            .externalReferenceNumber("external ref")
            .externalId(UUID.randomUUID().toString())
            .feeAccountNumber("PBA1234567")
            .feeCode("X1202")
            .reason("Reason for the case")
            .preferredCourt("London Court")
            .claimants(singletonList(CCDCollectionElement.<CCDParty>builder().value(getCCDPartyIndividual()).build()))
            .defendants(singletonList(CCDCollectionElement.<CCDParty>builder().value(getCCDPartyIndividual()).build()))
            .build();
    }

    public static CCDClaim getCCDCitizenClaim() {
        return CCDClaim.builder()
            .amount(
                CCDAmount.builder()
                    .type(BREAK_DOWN)
                    .amountBreakDown(
                        CCDAmountBreakDown.builder()
                            .rows(singletonList(
                                CCDCollectionElement.<CCDAmountRow>builder()
                                    .value(CCDAmountRow.builder()
                                        .amount(BigDecimal.valueOf(50))
                                        .reason("payment")
                                        .build())
                                    .build()))
                            .build()
                    )
                    .build())
            .payment(getCCDPayment())
            .interest(getCCDInterest())
            .statementOfTruth(getCCDStatementOfTruth())
            .externalReferenceNumber("external ref")
            .externalId(UUID.randomUUID().toString())
            .feeCode("X1202")
            .feeAmountInPennies(BigInteger.valueOf(400))
            .reason("Reason for the case")
            .claimants(singletonList(CCDCollectionElement.<CCDParty>builder().value(getCCDPartyIndividual()).build()))
            .defendants(singletonList(CCDCollectionElement.<CCDParty>builder().value(getCCDPartyIndividual()).build()))
            .build();
    }

    public static CCDInterestDate getCCDInterestDate() {
        return CCDInterestDate.builder()
            .date(LocalDate.now())
            .reason("reason")
            .type(CCDInterestDateType.CUSTOM)
            .build();
    }

    public static CCDInterest getCCDInterest() {
        return CCDInterest.builder()
            .rate(BigDecimal.valueOf(2))
            .reason("reason")
            .type(CCDInterestType.DIFFERENT)
            .interestDate(getCCDInterestDate())
            .build();
    }

    public static CCDPayment getCCDPayment() {
        return CCDPayment.builder()
            .id("paymentId")
            .reference("reference")
            .amount(BigDecimal.valueOf(7000))
            .dateCreated("2017-10-12")
            .status("Success")
            .build();
    }

    public static CCDHousingDisrepair getCCDHousingDisrepair() {
        return CCDHousingDisrepair.builder()
            .otherDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .costOfRepairsDamages(THOUSAND_POUNDS_OR_LESS.name())
            .build();
    }

    public static CCDPersonalInjury getCCDPersonalInjury() {
        return CCDPersonalInjury.builder()
            .generalDamages(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS.name())
            .build();
    }

    public static CCDStatementOfTruth getCCDStatementOfTruth() {
        return CCDStatementOfTruth
            .builder()
            .signerName("name")
            .signerRole("role")
            .build();
    }

    public static CCDOrganisation getCCDOrganisation() {
        CCDAddress ccdAddress = getCCDAddress();
        CCDContactDetails ccdContactDetails = getCCDContactDetails();
        CCDRepresentative ccdRepresentative = getCCDRepresentative(ccdAddress, ccdContactDetails);
        return CCDOrganisation.builder()
            .name("Xyz & Co")
            .address(ccdAddress)
            .phoneNumber("07987654321")
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .contactPerson("MR. Hyde")
            .companiesHouseNumber("12345678")
            .build();
    }

    public static CCDSoleTrader getCCDSoleTrader() {
        CCDAddress ccdAddress = getCCDAddress();
        CCDContactDetails ccdContactDetails = getCCDContactDetails();
        CCDRepresentative ccdRepresentative = getCCDRepresentative(ccdAddress, ccdContactDetails);
        return CCDSoleTrader.builder()
            .title("Mr.")
            .name("Individual")
            .phoneNumber("07987654321")
            .businessName("My Trade")
            .address(ccdAddress)
            .correspondenceAddress(ccdAddress)
            .representative(ccdRepresentative)
            .build();
    }

    public static CCDFullDefenceResponse getFullDefenceResponse() {
        return CCDFullDefenceResponse.builder()
            .moreTimeNeededOption(CCDYesNoOption.YES)
            .defence("My defence")
            .defenceType(CCDDefenceType.DISPUTE)
            .defendant(getCCDPartyIndividual())
            .build();

    }

    public static CCDResponse getCCDResponse() {
        return CCDResponse.builder()
            .responseType(CCDResponseType.FULL_DEFENCE)
            .fullDefenceResponse(getFullDefenceResponse())
            .build();
    }

    public static CCDClaimantResponse getCCDClaimantAcceptanceResponse(CCDFormaliseOption formaliseOption) {
        return CCDClaimantResponse.builder()
            .claimantResponseType(CCDClaimantResponseType.ACCEPTATION)
            .responseAcceptation(getResponseAcceptation(formaliseOption))
            .build();
    }

    public static CCDClaimantResponse getCCDClaimantREjectionResponse() {
        return CCDClaimantResponse.builder()
            .claimantResponseType(CCDClaimantResponseType.REJECTION)
            .responseRejection(getResponseRejection())
            .build();
    }

    public static CCDResponseAcceptation getResponseAcceptation(CCDFormaliseOption formaliseOption) {
        return CCDResponseAcceptation.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .claimantPaymentIntention(getCCDPaymentIntention())
            .courtDetermination(getCCDCourtDetermination())
            .formaliseOption(formaliseOption)
            .build();
    }

    public static CCDResponseRejection getResponseRejection() {
        return CCDResponseRejection.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .freeMediationOption(CCDYesNoOption.YES)
            .reason("Rejection Reason")
            .build();
    }

    public static CCDCourtDetermination getCCDCourtDetermination() {
        return CCDCourtDetermination.builder()
            .rejectionReason("Rejection reason")
            .courtPaymentIntention(getCCDPaymentIntention())
            .courtDecision(getCCDPaymentIntention())
            .disposableIncome(BigDecimal.valueOf(300))
            .decisionType(DecisionType.COURT)
            .build();
    }

    private static CCDPaymentIntention getCCDPaymentIntention() {
        return CCDPaymentIntention.builder()
            .paymentDate(LocalDate.of(2017, 10, 12))
            .paymentOption(CCDPaymentOption.BY_SPECIFIED_DATE)
            .repaymentPlan(getCCDRepaymentplan())
            .build();
    }

    private static CCDRepaymentPlan getCCDRepaymentplan() {
        return CCDRepaymentPlan.builder()
            .firstPaymentDate(LocalDate.of(2017, 10, 12))
            .instalmentAmount(BigDecimal.valueOf(123.98))
            .paymentSchedule(CCDPaymentSchedule.EACH_WEEK)
            .completionDate(LocalDate.of(2018, 10, 12))
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

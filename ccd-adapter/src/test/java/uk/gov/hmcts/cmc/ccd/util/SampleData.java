package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDisabilityStatus;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDLivingPartner;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDDecisionType.COURT;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption.SETTLEMENT;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDAgeGroupType.UNDER_11;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccountType.SAVINGS_ACCOUNT;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDisabilityStatus.SEVERE;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpenseType.COUNCIL_TAX;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncomeType.JOB;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency.MONTH;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebtType.ELECTRICITY;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDResidenceType.JOINT_OWN_HOME;
import static uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType.EXPERT_WITNESS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

public class SampleData {

    //Utility class
    private SampleData() {
    }

    public static CCDResponseAcceptation getResponseAcceptation(CCDFormaliseOption formaliseOption) {
        return CCDResponseAcceptation.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .claimantPaymentIntention(getCCDPaymentIntention())
            .submittedOn(LocalDateTimeFactory.nowInLocalZone())
            .formaliseOption(formaliseOption)
            .build();
    }

    public static CCDResponseAcceptation getResponseAcceptationWithClaimantPaymentIntentionImmediately() {
        return CCDResponseAcceptation.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .claimantPaymentIntention(getCCDPaymentIntentionImmediately())
            .submittedOn(LocalDateTimeFactory.nowInLocalZone())
            .formaliseOption(SETTLEMENT)
            .build();
    }

    public static CCDResponseAcceptation getResponseAcceptationWithClaimantPaymentIntentionPayBySetDate() {
        return CCDResponseAcceptation.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .claimantPaymentIntention(getCCDPaymentIntentionPayBySetDate())
            .submittedOn(LocalDateTimeFactory.nowInLocalZone())
            .formaliseOption(SETTLEMENT)
            .build();
    }

    public static CCDResponseRejection getResponseRejection() {
        return CCDResponseRejection.builder()
            .amountPaid(BigDecimal.valueOf(123.98))
            .submittedOn(LocalDateTimeFactory.nowInLocalZone())
            .freeMediationOption(YES)
            .mediationPhoneNumber("07999999999")
            .mediationContactPerson("Mediation Contact Person")
            .reason("Rejection Reason")
            .build();
    }

    public static CCDCourtDetermination getCCDCourtDetermination() {
        return CCDCourtDetermination.builder()
            .rejectionReason("Rejection reason")
            .courtIntention(getCCDPaymentIntention())
            .courtDecision(getCCDPaymentIntention())
            .disposableIncome(BigDecimal.valueOf(300))
            .decisionType(COURT)
            .build();
    }

    public static CCDCourtDetermination getCCDCourtDeterminationImmediately() {
        return CCDCourtDetermination.builder()
            .rejectionReason("Rejection reason")
            .courtIntention(getCCDPaymentIntentionImmediately())
            .courtDecision(getCCDPaymentIntention())
            .disposableIncome(BigDecimal.valueOf(300))
            .decisionType(COURT)
            .build();
    }

    public static CCDCourtDetermination getCCDCourtDeterminationPayBySetDate() {
        return CCDCourtDetermination.builder()
            .rejectionReason("Rejection reason")
            .courtIntention(getCCDPaymentIntentionPayBySetDate())
            .courtDecision(getCCDPaymentIntentionPayBySetDate())
            .disposableIncome(BigDecimal.valueOf(300))
            .decisionType(COURT)
            .build();
    }

    public static CCDPaymentIntention getCCDPaymentIntention() {
        return CCDPaymentIntention.builder()
            .paymentDate(LocalDate.of(2017, 10, 12))
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .firstPaymentDate(LocalDate.of(2017, 10, 12))
            .instalmentAmount(BigDecimal.valueOf(123.98))
            .paymentSchedule(CCDPaymentSchedule.EACH_WEEK)
            .completionDate(LocalDate.of(2018, 10, 12))
            .build();
    }

    private static CCDPaymentIntention getCCDPaymentIntentionImmediately() {
        return CCDPaymentIntention.builder()
            .paymentDate(LocalDate.now())
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .build();
    }

    private static CCDPaymentIntention getCCDPaymentIntentionPayBySetDate() {
        return CCDPaymentIntention.builder()
            .paymentDate(LocalDate.now().plusDays(10))
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .build();
    }

    public static List<CCDCollectionElement<CCDAmountRow>> getAmountBreakDown() {
        return singletonList(CCDCollectionElement.<CCDAmountRow>builder().value(CCDAmountRow.builder()
            .amount(BigDecimal.valueOf(50))
            .reason("payment")
            .build()).build());
    }

    public static CCDAddress getCCDAddress() {
        return CCDAddress.builder()
            .addressLine1("line1")
            .addressLine2("line2")
            .addressLine3("line3")
            .postTown("city")
            .postCode("postcode")
            .build();
    }

    public static CCDDefendant getCCDDefendantIndividual() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(INDIVIDUAL)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedName("Individual")
            .claimantProvidedDateOfBirth(LocalDate.of(1950, 01, 01))
            .claimantProvidedServiceAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationName("My Org")
            .claimantProvidedRepresentativeOrganisationPhone("07987654321")
            .claimantProvidedRepresentativeOrganisationEmail("my@email.com")
            .claimantProvidedRepresentativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDDefendant getCCDDefendantOrganisation() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(ORGANISATION)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedName("Organisation")
            .claimantProvidedServiceAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationName("My Org")
            .claimantProvidedRepresentativeOrganisationPhone("07987654321")
            .claimantProvidedRepresentativeOrganisationEmail("my@email.com")
            .claimantProvidedRepresentativeOrganisationDxAddress("dx123")
            .claimantProvidedContactPerson("MR. Hyde")
            .claimantProvidedCompaniesHouseNumber("12345678")
            .build();
    }

    public static CCDDefendant getCCDDefendantCompany() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(COMPANY)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedName("Abc Ltd")
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedServiceAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationName("My Org")
            .claimantProvidedRepresentativeOrganisationPhone("07987654321")
            .claimantProvidedRepresentativeOrganisationEmail("my@email.com")
            .claimantProvidedRepresentativeOrganisationDxAddress("dx123")
            .claimantProvidedContactPerson("MR. Hyde")
            .build();
    }

    public static CCDDefendant getCCDDefendantSoleTrader() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(SOLE_TRADER)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedTitle("Mr.")
            .claimantProvidedName("SoleTrader")
            .claimantProvidedBusinessName("My Trade")
            .claimantProvidedServiceAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationAddress(ccdAddress)
            .claimantProvidedRepresentativeOrganisationName("My Org")
            .claimantProvidedRepresentativeOrganisationPhone("07987654321")
            .claimantProvidedRepresentativeOrganisationEmail("my@email.com")
            .claimantProvidedRepresentativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDClaimant getCCDClaimantIndividual() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDClaimant.builder()
            .partyType(INDIVIDUAL)
            .partyAddress(ccdAddress)
            .partyName("Individual")
            .partyPhone("07987654321")
            .partyDateOfBirth(LocalDate.of(1950, 01, 01))
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail("my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDClaimant getCCDClaimantCompany() {
        CCDAddress ccdAddress = getCCDAddress();

        return CCDClaimant.builder()
            .partyType(COMPANY)
            .partyName("Abc Ltd")
            .partyAddress(ccdAddress)
            .partyPhone("07987654321")
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail("my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .partyContactPerson("MR. Hyde")
            .build();
    }

    public static CCDClaimant getCCDClaimantOrganisation() {
        CCDAddress ccdAddress = getCCDAddress();

        return CCDClaimant.builder()
            .partyType(ORGANISATION)
            .partyName("Xyz & Co")
            .partyAddress(ccdAddress)
            .partyPhone("07987654321")
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail("my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .partyContactPerson("MR. Hyde")
            .partyCompaniesHouseNumber("12345678")
            .build();
    }

    public static CCDClaimant getCCDClaimantSoleTrader() {
        CCDAddress ccdAddress = getCCDAddress();

        return CCDClaimant.builder()
            .partyType(SOLE_TRADER)
            .partyTitle("Mr.")
            .partyName("Individual")
            .partyBusinessName("My Trade")
            .partyPhone("07987654321")
            .partyAddress(ccdAddress)
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail("my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDCase getCCDLegalCase() {
        List<CCDCollectionElement<CCDClaimant>> claimants
            = singletonList(CCDCollectionElement.<CCDClaimant>builder().value(getCCDClaimantIndividual()).build());
        List<CCDCollectionElement<CCDDefendant>> defendants
            = singletonList(CCDCollectionElement.<CCDDefendant>builder().value(getCCDDefendantIndividual()).build());
        return CCDCase.builder()
            .id(1L)
            .submittedOn(LocalDateTime.of(2017, 11, 01, 10, 15, 30))
            .issuedOn(LocalDate.of(2017, 11, 15))
            .submitterEmail("my@email.com")
            .submitterId("123")
            .referenceNumber("ref no")
            .externalId(UUID.randomUUID().toString())
            .features("admissions")
            .amountType(RANGE)
            .amountLowerValue(BigDecimal.valueOf(50))
            .amountHigherValue(BigDecimal.valueOf(500))
            .housingDisrepairCostOfRepairDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .housingDisrepairOtherDamages(THOUSAND_POUNDS_OR_LESS.name())
            .personalInjuryGeneralDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .sotSignerName("name")
            .sotSignerRole("role")
            .externalReferenceNumber("external ref")
            .externalId(UUID.randomUUID().toString())
            .feeAccountNumber("PBA1234567")
            .feeCode("X1202")
            .reason("Reason for the case")
            .preferredCourt("London Court")
            .claimants(claimants)
            .defendants(defendants)
            .build();
    }

    public static CCDCase getCCDCitizenCase(List<CCDCollectionElement<CCDAmountRow>> amountBreakDown) {
        List<CCDCollectionElement<CCDClaimant>> claimants
            = singletonList(CCDCollectionElement.<CCDClaimant>builder().value(getCCDClaimantIndividual()).build());
        List<CCDCollectionElement<CCDDefendant>> defendants
            = singletonList(CCDCollectionElement.<CCDDefendant>builder().value(getCCDDefendantIndividual()).build());

        return CCDCase.builder()
            .id(1L)
            .submittedOn(LocalDateTime.of(2017, 11, 01, 10, 15, 30))
            .issuedOn(LocalDate.of(2017, 11, 15))
            .submitterEmail("my@email.com")
            .submitterId("123")
            .referenceNumber("ref no")
            .externalId(UUID.randomUUID().toString())
            .features("admissions")
            .amountType(BREAK_DOWN)
            .amountBreakDown(amountBreakDown)
            .housingDisrepairCostOfRepairDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .housingDisrepairOtherDamages(THOUSAND_POUNDS_OR_LESS.name())
            .personalInjuryGeneralDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .sotSignerName("name")
            .sotSignerRole("role")
            .externalReferenceNumber("external ref")
            .externalId(UUID.randomUUID().toString())
            .feeCode("X1202")
            .feeAmountInPennies(BigInteger.valueOf(100))
            .reason("Reason for the case")
            .preferredCourt("London Court")
            .interestType(CCDInterestType.DIFFERENT)
            .interestReason("reason")
            .interestRate(BigDecimal.valueOf(2))
            .interestBreakDownAmount(BigDecimal.valueOf(210))
            .interestBreakDownExplanation("Explanation")
            .interestStartDateReason("start date reason")
            .interestDateType(CCDInterestDateType.CUSTOM)
            .interestClaimStartDate(LocalDate.now())
            .interestSpecificDailyAmount(BigDecimal.valueOf(10))
            .interestEndDateType(CCDInterestEndDateType.SUBMISSION)
            .paymentStatus("success")
            .paymentDateCreated(LocalDate.of(2019, 01, 01))
            .paymentId("PaymentId")
            .paymentAmount(BigDecimal.valueOf(4000))
            .paymentReference("RC-1524-6488-1670-7520")
            .claimants(claimants)
            .defendants(defendants)
            .timeline(singletonList(CCDCollectionElement.<CCDTimelineEvent>builder()
                .value(CCDTimelineEvent.builder().date("some Date").description("description of event").build())
                .build()))
            .evidence(singletonList(CCDCollectionElement.<CCDEvidenceRow>builder()
                .value(CCDEvidenceRow.builder().type(EXPERT_WITNESS).description("description of evidence").build())
                .build()))
            .build();
    }

    public static CCDStatementOfMeans getCCDStatementOfMeans() {
        return CCDStatementOfMeans.builder()
            .residenceType(JOINT_OWN_HOME)
            .residenceOtherDetail("other details")
            .noOfMaintainedChildren(1)
            .anyDisabledChildren(NO)
            .numberOfOtherDependants(1)
            .otherDependantDetails("other details")
            .otherDependantAnyDisabled(NO)
            .taxPaymentsReason("reason")
            .taxYouOwe(TEN)
            .selfEmploymentJobTitle("Job title")
            .selfEmploymentAnnualTurnover(TEN)
            .unEmployedNoOfMonths(2)
            .employmentDetails("Details")
            .unEmployedNoOfYears(0)
            .dependantChildren(singletonList(
                CCDCollectionElement.<CCDChildCategory>builder().value(CCDChildCategory.builder()
                    .ageGroupType(UNDER_11)
                    .numberOfChildren(2)
                    .numberOfResidentChildren(2)
                    .build()
                ).build()
            ))
            .incomes(singletonList(
                CCDCollectionElement.<CCDIncome>builder().value(CCDIncome.builder()
                    .type(JOB)
                    .frequency(MONTH)
                    .amountReceived(TEN)
                    .build()
                ).build()
            ))
            .expenses(singletonList(
                CCDCollectionElement.<CCDExpense>builder().value(CCDExpense.builder()
                    .type(COUNCIL_TAX)
                    .frequency(MONTH)
                    .amountPaid(TEN)
                    .build()
                ).build()
            ))
            .debts(singletonList(
                CCDCollectionElement.<CCDDebt>builder().value(CCDDebt.builder()
                    .totalOwed(TEN)
                    .description("Reference")
                    .monthlyPayments(ONE)
                    .build()
                ).build()
            ))
            .bankAccounts(singletonList(
                CCDCollectionElement.<CCDBankAccount>builder().value(CCDBankAccount.builder()
                    .balance(BigDecimal.valueOf(100))
                    .joint(NO)
                    .type(SAVINGS_ACCOUNT)
                    .build()
                ).build()
            ))
            .courtOrders(singletonList(
                CCDCollectionElement.<CCDCourtOrder>builder().value(CCDCourtOrder.builder()
                    .amountOwed(TEN)
                    .claimNumber("Reference")
                    .monthlyInstalmentAmount(ONE)
                    .build()
                ).build()
            ))
            .priorityDebts(singletonList(
                CCDCollectionElement.<CCDPriorityDebt>builder().value(CCDPriorityDebt.builder()
                    .frequency(MONTH)
                    .amount(BigDecimal.valueOf(132.89))
                    .type(ELECTRICITY)
                    .build()
                ).build()
            ))
            .carer(YES)
            .livingPartner(CCDLivingPartner.builder()
                .disability(SEVERE)
                .over18(YES)
                .pensioner(YES)
                .build()
            )
            .disabilityStatus(CCDDisabilityStatus.YES)
            .retired(NO)
            .build();
    }
}

package uk.gov.hmcts.cmc.ccd;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestEndDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

public class SampleData {
    //Utility class
    private SampleData() {
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

    public static CCDDefendant getCCDDefendantIndividual() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(INDIVIDUAL)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedName("Individual")
            .claimantProvidedDateOfBirth(LocalDate.of(1950, 01, 01))
            .claimantProvidedServiceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDDefendant getCCDDefendantOrganisation() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDDefendant.builder()
            .claimantProvidedType(ORGANISATION)
            .claimantProvidedAddress(ccdAddress)
            .claimantProvidedName("Organisation")
            .claimantProvidedServiceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
            .representativeOrganisationDxAddress("dx123")
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
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
            .representativeOrganisationDxAddress("dx123")
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
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDClaimant getCCDClaimantIndividual() {
        CCDAddress ccdAddress = getCCDAddress();
        return CCDClaimant.builder()
            .partyType(INDIVIDUAL)
            .partyAddress(ccdAddress)
            .partyName("Individual")
            .partyPhoneNumber("07987654321")
            .partyDateOfBirth(LocalDate.of(1950, 01, 01))
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
            .representativeOrganisationDxAddress("dx123")
            .build();
    }

    public static CCDClaimant getCCDClaimantCompany() {
        CCDAddress ccdAddress = getCCDAddress();

        return CCDClaimant.builder()
            .partyType(COMPANY)
            .partyName("Abc Ltd")
            .partyAddress(ccdAddress)
            .partyPhoneNumber("07987654321")
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
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
            .partyPhoneNumber("07987654321")
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
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
            .partyPhoneNumber("07987654321")
            .partyAddress(ccdAddress)
            .partyCorrespondenceAddress(ccdAddress)
            .representativeOrganisationAddress(ccdAddress)
            .representativeOrganisationName("My Org")
            .representativeOrganisationPhone("07987654321")
            .representativeOrganisationEmail(",my@email.com")
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
            .feeAccountNumber("PBA1234567")
            .feeCode("X1202")
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
            .claimants(claimants)
            .defendants(defendants)
            .build();
    }

    public static List<CCDCollectionElement<CCDAmountRow>> getAmountBreakDown() {
        return singletonList(CCDCollectionElement.<CCDAmountRow>builder().value(CCDAmountRow.builder()
            .amount(BigDecimal.valueOf(50))
            .reason("payment")
            .build()).build());
    }

}

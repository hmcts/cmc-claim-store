package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDCompany;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDHousingDisrepair;
import uk.gov.hmcts.cmc.ccd.domain.CCDIndividual;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDateType;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestType;
import uk.gov.hmcts.cmc.ccd.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.ccd.domain.CCDPersonalInjury;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.ccd.domain.CCDSoleTrader;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
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

}

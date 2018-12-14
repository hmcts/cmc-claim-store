package uk.gov.hmcts.cmc.ccd.util;


import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPaymentIntention;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.RANGE;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDPartyType.*;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;
import static uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation.THOUSAND_POUNDS_OR_LESS;

public class SampleData {

    //Utility class
    private SampleData() {
    }

   /* public static CCDIndividual getCCDIndividual() {
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

    private static CCDRepresentative getCCDRepresentative(CCDAddress ccdAddress, CCDContactDetails ccdContactDetails) {
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

    private static CCDInterestDate getCCDInterestDate() {
        return CCDInterestDate.builder()
            .date(LocalDate.now())
            .reason("reason")
            .type(CCDInterestDateType.CUSTOM)
            .build();
    }

    private static CCDInterest getCCDInterest() {
        return CCDInterest.builder()
            .rate(BigDecimal.valueOf(2))
            .reason("reason")
            .type(CCDInterestType.DIFFERENT)
            .interestDate(getCCDInterestDate())
            .build();
    }

    private static CCDPayment getCCDPayment() {
        return CCDPayment.builder()
            .id("paymentId")
            .reference("reference")
            .amount(BigDecimal.valueOf(7000))
            .dateCreated("2017-10-12")
            .status("Success")
            .build();
    }

    private static CCDHousingDisrepair getCCDHousingDisrepair() {
        return CCDHousingDisrepair.builder()
            .otherDamages(MORE_THAN_THOUSAND_POUNDS.name())
            .costOfRepairsDamages(THOUSAND_POUNDS_OR_LESS.name())
            .build();
    }

    private static CCDPersonalInjury getCCDPersonalInjury() {
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

    private static CCDSoleTrader getCCDSoleTrader() {
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

    private static CCDFullDefenceResponse getFullDefenceResponse() {
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
    }*/

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
            .courtIntention(getCCDPaymentIntention())
            .courtDecision(getCCDPaymentIntention())
            .disposableIncome(BigDecimal.valueOf(300))
            .decisionType(DecisionType.COURT)
            .build();
    }

    private static CCDPaymentIntention getCCDPaymentIntention() {
        return CCDPaymentIntention.builder()
            .paymentDate(LocalDate.of(2017, 10, 12))
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .firstPaymentDate(LocalDate.of(2017, 10, 12))
            .instalmentAmount(BigDecimal.valueOf(123.98))
            .paymentSchedule(CCDPaymentSchedule.EACH_WEEK)
            .completionDate(LocalDate.of(2018, 10, 12))
            .build();
    }


}
